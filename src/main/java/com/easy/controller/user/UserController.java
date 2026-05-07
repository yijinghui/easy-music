package com.easy.controller.user;


import com.easy.pojo.dto.*;
import com.easy.pojo.vo.UserVO;
import com.easy.result.Result;
import com.easy.service.UserService;
import com.easy.utils.BindingResultUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController("userUserController")
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "C端-用户相关接口")
public class UserController {


    private final UserService userService;


    @PostMapping("/login/password")
    @Operation(summary = "用户密码登录接口")
    public Result loginByPassword(@RequestBody @Valid UserPasswordLoginDTO userLoginDTO, BindingResult bindingResult) {
        // 校验失败时，返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }

        return userService.loginByPassword(userLoginDTO);
    }

    @PostMapping("/login/email")
    @Operation(summary = "用户邮箱登录接口")
    public Result loginByEmail(@RequestBody @Valid UserEmailLoginDTO userLoginDTO, BindingResult bindingResult) {
        // 校验失败时，返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }

        return userService.loginByEmail(userLoginDTO);
    }


    @PostMapping("/register")
    @Operation(summary = "用户注册接口")
    public Result register(@RequestBody @Valid UserRegisterDTO userRegisterDTO, BindingResult bindingResult) {
        // 校验失败时，返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }

        return userService.register(userRegisterDTO);
    }

    @GetMapping("/sendVerificationCode/{optionType}")
    @Operation(summary = "发送验证码接口")
    public Result sendVerificationCode(
            @Parameter(description = "邮箱", required = true, example = "2199791686@qq.com")
            @RequestParam @Email String email,
            @PathVariable Integer optionType) {
        return userService.sendVerificationCode(email,optionType);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出接口")
    public Result logout() {
        return userService.logout();
    }



    @GetMapping("/getUserInfo")
    @Operation(summary = "获取用户信息接口")
    public Result<UserVO> getUserInfo() {
        return userService.userInfo();
    }

    @PutMapping("/updateUserInfo")
    @Operation(summary = "更新用户基本信息接口")
    public Result updateUserInfo(@RequestBody @Valid UserUpdateDTO updateDTO, BindingResult bindingResult) {
        // 校验失败时，返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }

        return userService.updateUserInfo(updateDTO);
    }

    @PutMapping("/updateUserPassword")
    @Operation(summary = "更新用户密码接口")
    public Result updateUserPassword(@RequestHeader("Authorization") String token,
                                     @RequestBody @Valid UserResetPasswordDTO userResetPasswordDTO,
                                     BindingResult bindingResult) {
        // 校验失败时，返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }

        return userService.updateUserPassword(userResetPasswordDTO);
    }


    @PutMapping("/updateUserEmail")
    @Operation(summary = "更新用户邮箱接口")
    public Result updateUserEmail(@RequestBody @Valid UserEmailUpdateDTO updateDTO, BindingResult bindingResult) {
        // 校验失败时，返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }

        return userService.updateUserEmail(updateDTO);
    }
    @PatchMapping("/updateUserAvatar")
    @Operation(summary = "更新用户头像接口")
    public Result updateUserAvatar(@RequestParam("avatar") MultipartFile avatar) {
        return userService.updateUserAvatar(avatar);
    }

    @PatchMapping("/resetUserPassword")
    @Operation(summary = "用户忘记密码接口")
    public Result resetUserPassword(@RequestBody @Valid UserResetPasswordDTO userResetPasswordDTO, BindingResult bindingResult) {
        // 校验失败时，返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }
        return userService.updateUserPassword(userResetPasswordDTO);
    }

    @Operation(summary = "用户注销接口")
    @DeleteMapping("/deleteAccount")
    public Result deleteAccount(@RequestHeader("Authorization") String token) {
        return userService.deleteAccount(token);
    }








}
