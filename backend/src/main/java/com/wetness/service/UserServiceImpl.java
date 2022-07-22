package com.wetness.service;

import com.wetness.model.User;
import com.wetness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final String hostKakao = "https://kauth.kakao.com/oauth/token";

    @Override
    @Transactional
    public void registerUser(User user) {
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateUser(Long id, User reqDto) {
        User user = userRepository.findOne(id);
        if(reqDto.getPassword()!=null){
            user.setPassword(reqDto.getPassword());
        }
        if(reqDto.getSidoCode()!=null){
            user.setSidoCode(reqDto.getSidoCode());
        }
        if(reqDto.getGugunCode()!=null){
            user.setGugunCode(reqDto.getGugunCode());
        }
        if(reqDto.getGender()!=null){
            user.setGender(reqDto.getGender());
        }
        if(reqDto.getHeight()!=0){
            user.setHeight(reqDto.getHeight());
        }
        if(reqDto.getWeight()!=0){
            user.setWeight(reqDto.getWeight());
        }

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
    public User getUserBySocialToken(int social, String socialToken) {

        return userRepository.findOneBySocial(social,socialToken);
    }

    @Override
    public String getSocialToken(int social, String code) throws IOException {
        URL url = new URL(hostKakao);
        switch (social){
            case 2 : url = new URL(hostKakao);
                break;
        }

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        String token = "";
        try {
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true); // 데이터 기록 알려주기

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("grant_type=authorization_code");
            // Client_id = REST_API_KEY 수정 필요
            sb.append("&client_id=cddf8fe3437329bf32a17342aa27ea7e");
            // 초기 프론트에서 설정한 Redirect_uri
            sb.append("&redirect_uri=http://localhost:8080/user/login/kakao");
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
            while((line=br.readLine())!=null)
            {
                res+=line;
            }

            System.out.println("res = " + res);


            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(res);
            JSONObject kakaoAccount = (JSONObject) obj.get("kakao_account");


            String id = obj.get("id").toString();
            String email = kakaoAccount.get("email").toString();
            String gender = kakaoAccount.get("gender").toString();

            result.put("id", id);
            result.put("email", email);
            result.put("gender", gender);

            br.close();


        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return result;
    }


}
