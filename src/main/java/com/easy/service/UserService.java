package com.easy.service;

import com.easy.pojo.dto.UserAddDTO;
import com.easy.pojo.dto.UserDTO;
import com.easy.pojo.dto.UserPageQueryDTO;
import com.easy.result.PageResult;
import com.easy.pojo.vo.UserAdminVO;
import com.easy.result.Result;
import jakarta.validation.Valid;

import java.util.List;

public interface UserService {
    Result<PageResult<UserAdminVO>> page(UserPageQueryDTO userPageQueryDTO);

    Result addUser(@Valid UserAddDTO userAddDTO);


    Result updateUser(@Valid UserDTO userDTO);

    Result updateUserStatus(Long userId, Integer userStatus);

    Result deleteUser(Long userId);

    Result deleteUsers(List<Long> userIds);
}
