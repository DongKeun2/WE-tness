package com.wetness.model.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wetness.auth.jwt.JwtUtil;
import com.wetness.db.entity.Follow;
import com.wetness.db.entity.LoggedContinue;
import com.wetness.db.entity.LoggedIn;
import com.wetness.db.entity.User;
import com.wetness.db.repository.*;
import com.wetness.exception.DropUserException;
import com.wetness.model.dto.request.JoinUserDto;
import com.wetness.model.dto.request.PasswordDto;
import com.wetness.model.dto.request.UpdateUserDto;
import com.wetness.model.dto.response.LoginDto;
import com.wetness.model.dto.response.LoginLogResDto;
import com.wetness.model.dto.response.UserInfoResDto;
import com.wetness.model.dto.response.LoginSocialDto;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final LoggedContinueRepository loggedContinueRepository;
    private final CommonCodeRepository commonCodeRepository;
    private final LoggedInRepository loggedInRepository;
    private final FollowRepository followRepository;

    private final AwardService awardService;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
//    @Value("${login.kakao.clientId}")
    private String kakaoClientId = "376a57e7cef85c6560a77351c93a9b1f";
//    @Value("${login.kakao.redirectUri}")
    private String kakaoRedirectUri = "https://i7a205.p.ssafy.io/login/kakao";

    @Override
    @Transactional
    public boolean registerUser(JoinUserDto joinUserDto) {
        if (!checkEmailDuplicate(joinUserDto.getEmail()) &&
                !checkNicknameDuplicate(joinUserDto.getNickname())) {
            User user = new User(
                    joinUserDto.getEmail(),
                    passwordEncoder.encode(joinUserDto.getPassword()),
                    joinUserDto.getNickname(),
                    "wetness",
                    "user"
            );
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public LoginSocialDto registerSocialUser(Map<String, Object> data) {

        RandomString randomString = new RandomString(20);
        User user = new User();

        user.setSocial("kakao");
        user.setSocialId((String) data.get("id"));
        // ?????? ????????? ??????
        user.setNickname(randomString.make());
        // email, gender ?????? ????????? ?????? ?????? ??????
        if (data.containsKey("email")) {
            user.setEmail((String) data.get("email"));
        } else {
            user.setEmail(randomString.make());
        }
        if (data.containsKey("gender")) {
            user.setGender((String) data.get("gender"));
        } else { // ?????? ??????
            user.setGender("3");
        }
        user.setRole("user");

        userRepository.save(user);

        LoginSocialDto loginSocialDto = loginSocialUser(user);
        loginSocialDto.setExistUser("false");

        return loginSocialDto;
    }

    @Override
    @Transactional
    public LoginDto setSocialAccount(UserDetailsImpl userDetails, String changedNickname) {

        User user = userRepository.getOne(userDetails.getId());
        user.setNickname(changedNickname);

        return new ObjectMapper().convertValue(loginSocialUser(user), LoginDto.class);
    }

    @Override
    @Transactional
    public boolean updateUser(Long id, UpdateUserDto updateUserDto) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            if (updateUserDto.getNickname() != null &&
                    !updateUserDto.getNickname().isEmpty() &&
                    !checkNicknameDuplicate(updateUserDto.getNickname())) {
                user.setNickname(updateUserDto.getNickname());
            }

            if (updateUserDto.getAddressCode() != null) {
                String inputAddressCode = updateUserDto.getAddressCode();
                if (inputAddressCode != null && inputAddressCode.length() == 10) {
                    user.setSidoCode(inputAddressCode.substring(0, 2) + "00000000");
                    user.setGugunCode(inputAddressCode.substring(0, 5) + "00000");
                }
            }
            if (updateUserDto.getGender() != null && !updateUserDto.getGender().isEmpty()) {
                user.setGender(updateUserDto.getGender());
            }
            if (updateUserDto.getHeight() != null && updateUserDto.getHeight() != 0.0) {
                user.setHeight(updateUserDto.getHeight());
            }
            if (updateUserDto.getWeight() != null && updateUserDto.getWeight() != 0.0) {
                user.setWeight(updateUserDto.getWeight());
            }
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void updateUser(Long id, User reqDto) {
        User user = userRepository.getOne(id);
        System.out.println("???????????? : " + user.getId());
        if (reqDto.getPassword() != null) {
            user.setPassword(reqDto.getPassword());
        }
        if (reqDto.getSidoCode() != null) {
            user.setSidoCode(reqDto.getSidoCode());
        }
        if (reqDto.getGugunCode() != null) {
            user.setGugunCode(reqDto.getGugunCode());
        }
        if (reqDto.getGender() != null) {
            user.setGender(reqDto.getGender());
        }
        if (reqDto.getHeight() != 0) {
            user.setHeight(reqDto.getHeight());
        }
        if (reqDto.getWeight() != 0) {
            user.setWeight(reqDto.getWeight());
        }

    }


    @Override
    @Transactional
    public void saveRefreshToken(String nickname, String refreshToken) {
        User findUser = userRepository.findByNickname(nickname);
        if (findUser != null) {
            findUser.setRefreshToken(refreshToken);
        }
    }

    @Override
    public String getRefreshToken(String nickname) {
        User user = userRepository.findByNickname(nickname);
        return user.getRefreshToken();
    }

    @Override
    @Transactional
    public boolean deleteUser(String nickname) {
        User user = userRepository.findByNickname(nickname);
        if (user != null) {
            user.setRole("drop");
            //?????? ????????? ????????? ?????? ?????? ????????? ??????
            ArrayList<Follow> byFollowing = followRepository.findByFollowing(user);
            followRepository.deleteAll(byFollowing);

            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public User findByNickname(String nickname) {

        return userRepository.findByNickname(nickname);

    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email + "??? ???????????? ??????????????? ????????????"));
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public boolean checkEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public boolean checkNicknameDuplicate(String nickname) {
        return userRepository.existsByNickname(nickname);
    }


    @Override
    public String getSocialAccessToken(String code) throws IOException {


        URL url = new URL("https://kauth.kakao.com/oauth/token");

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        String token = "";
        System.out.println("kakao Client Id " + kakaoClientId);
        System.out.println("redirect url " + kakaoRedirectUri);
        try {
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true); // ????????? ?????? ????????????

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("grant_type=authorization_code");
            // Client_id = REST_API_KEY ?????? ??????
            sb.append("&client_id=" + kakaoClientId);
            // ?????? ??????????????? ????????? Redirect_uri
            sb.append("&redirect_uri="+kakaoRedirectUri);
            sb.append("&code=" + code);

            bw.write(sb.toString());
            bw.flush();

            int responseCode = urlConnection.getResponseCode();
            System.out.println("responseCode = " + responseCode);

            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line = "";
            String result = "";
            while ((line = br.readLine()) != null) {
                result += line;
            }
            System.out.println("result = " + result);

            // json parsing
            JSONParser parser = new JSONParser();
            JSONObject elem = (JSONObject) parser.parse(result);

            String access_token = elem.get("access_token").toString();
            String refresh_token = elem.get("refresh_token").toString();
            System.out.println("refresh_token = " + refresh_token);
            System.out.println("access_token = " + access_token);

            token = access_token;

            br.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return token;
    }

    public Map<String, Object> getUserInfo(String accessToken) throws IOException {
        String host = "https://kapi.kakao.com/v2/user/me";
        Map<String, Object> result = new HashMap<>();
        try {
            URL url = new URL(host);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Bearer " + accessToken);
            urlConnection.setRequestMethod("GET");

            int responseCode = urlConnection.getResponseCode();
            System.out.println("responseCode = " + responseCode);


            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line = "";
            String res = "";
            while ((line = br.readLine()) != null) {
                res += line;
            }

            System.out.println("res = " + res);


            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(res);
            JSONObject kakaoAccount = (JSONObject) obj.get("kakao_account");

            // id??? ????????? kakao account ?????? ?????? ?????????
            String id = obj.get("id").toString();
            result.put("id", id);
            for (Object key : kakaoAccount.keySet()) {
                result.put((String) key, kakaoAccount.get(key));
            }


            br.close();


        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public Optional<User> socialLogin(Map<String, Object> data) {

        String id = data.get("id").toString();

        return userRepository.findBySocialAndSocialId("kakao", id);

    }


    @Override
    @Transactional
    public void logoutUser(String nickname) {
        User user = findByNickname(nickname);
        if (user != null) {
            user.setRefreshToken(null);
        }
    }

    @Override
    @Transactional
    public void setLoginData(Long userId) {
        LocalDate today = LocalDate.now();
        LoggedContinue loggedContinue = loggedContinueRepository.findByUserId(userId);
        if (loggedContinue == null) {
            loggedContinue = new LoggedContinue(userId, 1, 1, today);
            loggedContinueRepository.save(loggedContinue);
        } else if (!today.isEqual(loggedContinue.getRecentDate())) {
            if (today.isEqual(loggedContinue.getRecentDate().plusDays(1))) {
                loggedContinue.setConsecutively(loggedContinue.getConsecutively() + 1);
                if (loggedContinue.getMaxConsecutively() < loggedContinue.getConsecutively()) {
                    loggedContinue.setMaxConsecutively(loggedContinue.getConsecutively());
                }
            } else { //????????? ????????? ?????? ?????? ?????? ?????? ????????? 1??? ??????
                loggedContinue.setConsecutively(1);
            }
            loggedContinue.setRecentDate(today);
        }
    }


    @Override
    public LoggedContinue getLoginData(Long userId) {
        return loggedContinueRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public boolean updateUserPassword(long id, PasswordDto passwordDto) {
        User user = userRepository.getOne(id);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(passwordDto.getPassword()));
            return true;
        }
        return false;
    }

    //TODO security Role ???????????? drop??? ????????? ?????? ?????? ?????? ??????
    @Override
    @Transactional
    public LoginDto loginUser(User user) {
        Authentication authentication = getAuthentication(user);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        if (userDetails.getRole().equals("drop")) {
            throw new DropUserException();
        }

        String accessToken = jwtUtil.createAccessToken(authentication);
        String refreshToken = jwtUtil.createRefreshToken();

        saveRefreshToken(userDetails.getNickname(), refreshToken);
        setLoginData(userDetails.getId());
        setLoggedInData(userDetails.getId());

        awardService.awardCheckLogin(userDetails.getId());

        return new LoginDto("200", null, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public LoginSocialDto loginSocialUser(User user) {

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);
        String accessToken = jwtUtil.createAccessToken(authentication);
        String refreshToken = jwtUtil.createRefreshToken();

        saveRefreshToken(userDetails.getNickname(), refreshToken);
        setLoginData(userDetails.getId());

        return new LoginSocialDto("true", "200", null, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public void setLoggedInData(long userId) {
        User user = findById(userId);
        if (user != null) {
            LoggedIn loggedIn = new LoggedIn();
            loggedIn.setUser(user);
            loggedIn.setDate(LocalDateTime.now());
            loggedInRepository.save(loggedIn);
        }
    }

    @Override
    public ArrayList<LoginLogResDto> getLoginLog(long userId) {
        return loggedInRepository.getLoginLog(userId);
    }

    @Override
    public ArrayList<String> getLoginDateLog(long userId) {
        return loggedInRepository.getLoginDateLog(userId);
    }


    @Override
    public Authentication getAuthentication(User user) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    @Override
    public LoginDto getCurrentUserLoginDto(String headerAuth, String nickname) {
        String accessToken = null;
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            accessToken = headerAuth.substring(7, headerAuth.length());
        }
        String refreshToken = getRefreshToken(nickname);
        return new LoginDto("200", null, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public ArrayList<UserInfoResDto> getUsersInfoResDto(ArrayList<String> users) {
        ArrayList<UserInfoResDto> list = new ArrayList<UserInfoResDto>();
        for (String userNickname : users) {
            User user = userRepository.findByNickname(userNickname);
            String address = getAddress(user.getSidoCode(), user.getGugunCode());
            UserInfoResDto userInfoResDto = UserInfoResDto.generateUserInfoResDto(user, address);
            list.add(userInfoResDto);
        }
        return list;
    }

    @Override
    @Transactional
    public UserInfoResDto getUserInfoResDto(String nickname) {
        User user = findByNickname(nickname);
        if (user != null) {
            String address = getAddress(user.getSidoCode(), user.getGugunCode());
            return UserInfoResDto.generateUserInfoResDto(user, address);
        }
        return null;
    }

    @Override
    @Transactional
    public String getAddress(String sidoCode, String gugunCode) {
        if (sidoCode != null && gugunCode != null) {
            String sido = commonCodeRepository.findByCode(sidoCode).getName();
            String gugun = commonCodeRepository.findByCode(gugunCode).getName();
            return sido + " " + gugun;
        }
        return null;
    }

    @Override
    public ArrayList<UserInfoResDto> searchUserWithKeyword(String keyword) {
        ArrayList<UserInfoResDto> list = new ArrayList<UserInfoResDto>();
        ArrayList<User> users = userRepository.findByNicknameContains(keyword);

        for (User u : users) {
            String address = getAddress(u.getSidoCode(), u.getGugunCode());
            UserInfoResDto userInfoResDto = UserInfoResDto.generateUserInfoResDto(u, address);
            list.add(userInfoResDto);
        }
        return list;
    }
}
