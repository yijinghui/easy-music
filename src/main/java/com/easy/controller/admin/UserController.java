package com.easy.controller.admin;





import com.easy.annotation.LogOperation;
import com.easy.pojo.dto.UserDTO;
import com.easy.pojo.dto.UserPageQueryDTO;

import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin端-用户管理接口")
public class UserController {

    private final UserService userService;
    @Operation(summary = "用户分页查询接口")
    @PostMapping("/page")
    public Result<PageResult> getAllUsers(@RequestBody @Valid UserPageQueryDTO userPageQueryDTO) {
        return userService.getAllUsers(userPageQueryDTO);

    }

    @Operation(summary = "用户新增接口")
    @PostMapping("/create")
    @LogOperation
    public Result addUser(@RequestBody @Valid UserDTO userDTO) {
        return userService.addUser(userDTO);
    }

    @Operation(summary = "根据id更新用户信息接口")
    @PutMapping("/update")
    @LogOperation
    public Result updateUser(@RequestBody @Valid UserDTO userDTO) {
        userService.updateUserInfo(userDTO);
        return Result.success();
    }

    @Operation(summary = "更新用户状态接口")
    @LogOperation
    @PatchMapping("/status/{id}")
    public Result updateUserStatus(@PathVariable("id") Long userId, @PathVariable("status") Integer userStatus) {
        return userService.updateUserStatus(userId, userStatus);
    }


    @Operation(summary = "删除用户接口")
    @LogOperation
    @DeleteMapping("/{id}")
    public Result deleteUser(@PathVariable("id") Long userId) {
        return userService.deleteUser(userId);
    }


    @Operation(summary = "批量删除用户接口")
    @LogOperation
    @DeleteMapping("/batch")
    public Result deleteUsers(@RequestBody List<Long> userIds) {
        return userService.deleteUsers(userIds);
    }


    @PatchMapping("/avatar/{id}")
    @Operation(summary = "更新用户头像接口")
    @LogOperation
    public Result updateUserAvatar(@PathVariable("id") Long userId,@RequestParam("avatar") MultipartFile avatar) {
        userService.updateUserAvatar(userId,avatar);
        return Result.success();
    }






}
