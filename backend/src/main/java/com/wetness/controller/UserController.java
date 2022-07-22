package com.wetness.controller;

import com.wetness.jwt.JwtUtil;
import com.wetness.model.User;
import com.wetness.model.request.JoinUserDto;
import com.wetness.model.response.BaseResponseEntity;
import com.wetness.model.response.DuplicateCheckResDto;
import com.wetness.model.response.FindEmailResDto;
import com.wetness.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/join")
    @ApiOperation(value = "회원가입")
    public ResponseEntity<BaseResponseEntity> registerUser(@RequestBody JoinUserDto joinUserDto) {
        if (userService.registerUser(joinUserDto)) {
            return ResponseEntity.ok().body(new BaseResponseEntity(200, "Success"));
        }
        return ResponseEntity.internalServerError().body(new BaseResponseEntity(500, "Fail"));
    }

    //TODO sql error 처리 추가 필요
    @PostMapping("/login")
    @ApiOperation(value = "로그인")
    public ResponseEntity<BaseResponseEntity> loginUser(@RequestBody User user) {
        User loginUser = userService.findLoginUser(user.getEmail(), user.getPassword());
        if (loginUser != null) {
            String token = jwtUtil.createToken(loginUser);
            return ResponseEntity.ok().body(new BaseResponseEntity(200, token));
        }
        return ResponseEntity.badRequest().body(new BaseResponseEntity(400, "Fail"));
    }


    @GetMapping("/duplicatedEmail/{email}")
    @ApiOperation(value = "이메일 중복확인")
    public ResponseEntity<DuplicateCheckResDto> duplicatedEmail(@PathVariable String email) {
        boolean possible = userService.checkEmailDuplicate(email);

        //true면 사용가능, false면 이미 존재
        return ResponseEntity.ok().body(new DuplicateCheckResDto(possible));
    }

    @GetMapping("/duplicatedNickname/{nickname}")
    @ApiOperation(value = "닉네임 중복확인")
    public ResponseEntity<DuplicateCheckResDto> duplicatedNickname(@PathVariable String nickname) {
        boolean possible = userService.checkNicknameDuplicate(nickname);

        //true면 사용 가능, false면 이미 존재
        return ResponseEntity.ok().body(new DuplicateCheckResDto(possible));
    }

    @PutMapping
    @ApiOperation(value = "회원정보 수정")
    public ResponseEntity<BaseResponseEntity> updateUser(@RequestBody User user) {
        userService.updateUser(user.getId(), user);

        return ResponseEntity.ok().body(new BaseResponseEntity(200, "Success"));
    }

    @PostMapping("/findEmail")
    @ApiOperation(value="이메일 찾기")
    public ResponseEntity<FindEmailResDto> findId(@RequestParam("nickname") String nickname){
        System.out.println("sendPwd Nickname : "+nickname);

        FindEmailResDto resDto = userService.findByEmail(nickname);

        return ResponseEntity.status(200).body(resDto);
    }

}
