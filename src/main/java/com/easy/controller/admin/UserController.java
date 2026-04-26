package com.easy.controller.admin;





import com.easy.pojo.dto.UserAddDTO;
import com.easy.pojo.dto.UserDTO;
import com.easy.pojo.dto.UserPageQueryDTO;

import com.easy.pojo.vo.UserAdminVO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.UserService;
import com.easy.utils.BindingResultUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "用户管理接口")
public class UserController {

    private final UserService userService;
    @Operation(summary = "用户分页查询接口")
    @PostMapping("/getAllUsers")
    public Result<PageResult<UserAdminVO>> page(@RequestBody UserPageQueryDTO userPageQueryDTO) {
        return userService.page(userPageQueryDTO);
    }

    @Operation(summary = "用户新增接口")
    @PostMapping("/addUser")
    public Result addUser(@RequestBody @Valid UserAddDTO userAddDTO, BindingResult bindingResult) {
        // 校验失败时，返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }

        return userService.addUser(userAddDTO);
    }

    @Operation(summary = "根据id更新用户信息接口")
    @PutMapping("/updateUser")
    public Result updateUser(@RequestBody @Valid UserDTO userDTO, BindingResult bindingResult) {
        // 校验失败时，返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }

        return userService.updateUser(userDTO);
    }

    @Operation(summary = "更新用户状态接口")
    @PatchMapping("/updateUserStatus/{id}/{status}")
    public Result updateUserStatus(@PathVariable("id") Long userId, @PathVariable("status") Integer userStatus) {
        return userService.updateUserStatus(userId, userStatus);
    }


    @Operation(summary = "删除用户接口")
    @DeleteMapping("/deleteUser/{id}")
    public Result deleteUser(@PathVariable("id") Long userId) {
        return userService.deleteUser(userId);
    }


    @Operation(summary = "批量删除用户接口")
    @DeleteMapping("/deleteUsers")
    public Result deleteUsers(@RequestBody List<Long> userIds) {
        return userService.deleteUsers(userIds);
    }






}
