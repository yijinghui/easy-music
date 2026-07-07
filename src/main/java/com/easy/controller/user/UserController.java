package com.easy.controller.user;


import com.easy.pojo.dto.*;
import com.easy.pojo.vo.UserVO;
import com.easy.result.Result;
import com.easy.service.UserService;
import com.easy.utils.BindingResultUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController("userUserController")
@RequiredArgsConstructor
@Tag(name = "C端-用户相关接口")
public class UserController {


    private final UserService userService;


    @PostMapping("/login/password")
    @Operation(summary = "用户密码登录接口")
    public Result loginByPassword(@RequestBody @Valid UserPasswordLoginDTO userLoginDTO) {
        String token = userService.loginByPassword(userLoginDTO);
        return Result.success("登录成功", token);
    }

    @PostMapping("/login/email")
    @Operation(summary = "用户邮箱登录接口")
    public Result loginByEmail(@RequestBody @Valid UserEmailLoginDTO userLoginDTO) {

        String token = userService.loginByEmail(userLoginDTO);
        return Result.success("登录成功", token);
    }


    @PostMapping("/register")
    @Operation(summary = "用户注册接口")
    public Result register(@RequestBody @Valid UserRegisterDTO userRegisterDTO) {
        userService.register(userRegisterDTO);
        return Result.success("注册成功");
    }

    @GetMapping("/code/{optionType}/{email}")
    @Operation(summary = "发送验证码接口")
    public Result sendVerificationCode(
            @PathVariable @Email(message = "邮箱格式错误") String email,
            @PathVariable @Min(value = 1, message = "验证码类型必须在1-4之间")
            @Max(value = 4, message = "验证码类型必须在1-4之间") Integer optionType) {
        userService.sendVerificationCode(email,optionType);
        return Result.success("验证码发送成功");
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出接口")
    public Result logout() {
        userService.logout();
        return Result.success("退出登录成功");
    }



    @GetMapping("/me")
    @Operation(summary = "获取用户信息接口")
    public Result<UserVO> getUserInfo() {
        UserVO userVO = userService.userInfo();
        return Result.success(userVO);
    }

    @PutMapping("/me")
    @Operation(summary = "更新用户基本信息接口")
    public Result updateUserInfo(@RequestBody @Valid UserDTO userDTO) {
        userService.updateUserInfo(userDTO);
        return Result.success("更新成功");
    }

    @PutMapping("/password")
    @Operation(summary = "更新用户密码接口")
    public Result updateUserPassword(
        @RequestBody @Valid UserResetPasswordDTO userResetPasswordDTO) {
        userService.updateUserPassword(userResetPasswordDTO);
        return Result.success("更新成功");
    }


    @PutMapping("/email")
    @Operation(summary = "更新用户邮箱接口")
    public Result updateUserEmail(
            @RequestBody @Valid UserEmailUpdateDTO updateDTO) {
        userService.updateUserEmail(updateDTO);
        return Result.success("更新成功");
    }
    @PatchMapping("/avatar")
    @Operation(summary = "更新用户头像接口")
    public Result updateUserAvatar(@RequestParam MultipartFile avatar) {
        userService.updateUserAvatar(null,avatar);
        return Result.success("更新成功");
    }

    @PatchMapping("/password/reset")
    @Operation(summary = "用户忘记密码接口")
    public Result resetUserPassword(
            @RequestBody @Valid UserResetPasswordDTO userResetPasswordDTO) {
        userService.updateUserPassword(userResetPasswordDTO);
        return Result.success("更新成功");
    }

    @Operation(summary = "用户注销接口")
    @DeleteMapping("/account")
    public Result deleteAccount(@RequestHeader("Authorization") String token) {
        userService.deleteAccount(token);
        return Result.success("注销成功");
    }





}
