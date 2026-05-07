package com.easy.service;

import com.easy.pojo.dto.*;
import com.easy.pojo.vo.UserVO;
import com.easy.result.PageResult;
import com.easy.pojo.vo.UserAdminVO;
import com.easy.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    Result<PageResult<UserAdminVO>> page(UserPageQueryDTO userPageQueryDTO);

    Result addUser(@Valid UserAddDTO userAddDTO);


    Result updateUser(@Valid UserDTO userDTO);

    Result updateUserStatus(Long userId, Integer userStatus);

    Result deleteUser(Long userId);

    Result deleteUsers(List<Long> userIds);

    Result loginByPassword(@Valid UserPasswordLoginDTO userLoginDTO);

    Result register(@Valid UserRegisterDTO userRegisterDTO);

    Result sendVerificationCode(@Email String email,Integer operationType);

    Result logout();

    Result<UserVO> userInfo();

    Result updateUserInfo(@Valid UserUpdateDTO updateDTO);


    Result updateUserEmail(@Valid UserEmailUpdateDTO updateDTO);

    Result updateUserPassword(UserResetPasswordDTO updateDTO);

    Result loginByEmail(@Valid UserEmailLoginDTO userLoginDTO);

    Result updateUserAvatar(MultipartFile avatar);


    Result deleteAccount(String token);
}
