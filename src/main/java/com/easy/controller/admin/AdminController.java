package com.easy.controller.admin;


import com.easy.pojo.dto.AdminDTO;
import com.easy.pojo.dto.AdminUpdatePasswordDTO;
import com.easy.result.Result;
import com.easy.service.AdminService;
import com.easy.utils.BindingResultUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin")
@Tag(name = "管理员相关接口")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @Operation(summary = "管理员登录接口")
    @PostMapping("/login")
    public Result login(@RequestBody @Valid AdminDTO adminDTO, BindingResult bindingResult) {
        // 校验失败时，返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }

        return adminService.login(adminDTO);
    }


    @Operation(summary = "管理员登出接口")
    @PostMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String token) {
        return adminService.logout(token);
    }

    @Operation(summary = "管理员修改密码接口")
    @PostMapping("/updatePassword")
    public Result updatePassword(@RequestBody @Valid AdminUpdatePasswordDTO updatePasswordDTO, BindingResult bindingResult) {
        // 校验失败时，返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }

        return adminService.updatePassword(updatePasswordDTO);
    }



}
