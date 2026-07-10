package com.easy.controller.admin;

import com.easy.annotation.LogOperation;
import com.easy.pojo.dto.UserPageQueryDTO;
import com.easy.pojo.entity.User;
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

    @PostMapping("/list")
    @Operation(summary = "用户分页查询接口")
    public Result<PageResult> list(@RequestBody @Valid UserPageQueryDTO userPageQueryDTO) {
        return Result.success(userService.list(userPageQueryDTO));
    }

    @PostMapping("/add")
    @Operation(summary = "用户新增接口")
    @LogOperation
    public Result addUser(@RequestBody @Valid User user) {
        userService.save(user);
        return Result.success("新增成功");
    }

    @PutMapping("/update")
    @Operation(summary = "根据id更新用户信息接口")
    @LogOperation
    public Result update(@RequestBody @Valid User user) {
        userService.updateById(user);
        return Result.success("更新成功");
    }

    @PatchMapping("/{id}/{status}")
    @Operation(summary = "更新用户状态接口")
    @LogOperation
    public Result updateStatus(@PathVariable("id") Long userId,
                               @PathVariable("status") Integer userStatus) {
        userService.updateStatus(userId, userStatus);
        return Result.success("更新成功");
       }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户接口")
    @LogOperation
    public Result deleteUser(@PathVariable("id") Long userId) {
        userService.removeById(userId);
        return Result.success("删除成功");
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除用户接口")
    @LogOperation
    public Result deleteUsers(@RequestBody List<Long> userIds) {
        userService.removeByIds(userIds);
        return Result.success("删除成功");
    }

    @PatchMapping("/avatar/{id}")
    @Operation(summary = "更新用户头像接口")
    @LogOperation
    public Result updateAvatar(@PathVariable("id") Long userId,
                               @RequestParam("avatar") MultipartFile avatar) {
        userService.updateAvatar(userId, avatar);
        return Result.success("更新头像成功");
    }
}
