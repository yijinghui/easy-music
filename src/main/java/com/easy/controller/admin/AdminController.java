package com.easy.controller.admin;


import com.easy.annotation.LogOperation;
import com.easy.constant.MessageConstant;
import com.easy.pojo.dto.AdminDTO;
import com.easy.result.Result;
import com.easy.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin")
@Tag(name = "Admin端-管理员相关接口")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @Operation(summary = "管理员登录接口")
    @PostMapping("/login")
    public Result login(@RequestBody @Valid AdminDTO adminDTO) {
        String token = adminService.login(adminDTO);
        return Result.success("登录成功", token);
    }


    @Operation(summary = "管理员登出接口")
    @PostMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String token) {
        adminService.logout(token);
        return Result.success("注销成功！");
    }



    @Operation(summary = "管理员修改密码接口")
    @PutMapping("/password")
    @LogOperation
    public Result updatePassword(
            @RequestParam("newPassword")
            @NotBlank(message = MessageConstant.PASSWORD + MessageConstant.NOT_NULL)
            @Pattern(regexp = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z\\W]{8,18}$", message = MessageConstant.PASSWORD + MessageConstant.FORMAT_ERROR)
            @Schema(description = "管理员密码", example = "Hh12345678")
            String newPassword) {
        adminService.updatePassword(newPassword);
        return Result.success("密码修改成功！");
    }

}
