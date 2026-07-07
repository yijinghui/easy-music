package com.easy.service;

import com.easy.pojo.dto.*;
import com.easy.pojo.vo.UserVO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    Result<PageResult> getAllUsers(UserPageQueryDTO userPageQueryDTO);

    Result addUser(UserDTO userDTO);

    Result updateUserStatus(Long userId, Integer userStatus);

    Result deleteUser(Long userId);

    Result deleteUsers(List<Long> userIds);

    String loginByPassword(UserPasswordLoginDTO userLoginDTO);

    void register(UserRegisterDTO userRegisterDTO);

    void sendVerificationCode(String email,Integer operationType);

    void logout();

    UserVO userInfo();

    void updateUserInfo(@Valid UserDTO userDTO);


    void updateUserEmail(@Valid UserEmailUpdateDTO updateDTO);

    void updateUserPassword(UserResetPasswordDTO updateDTO);

    String loginByEmail(@Valid UserEmailLoginDTO userLoginDTO);

    void updateUserAvatar(Long userId,MultipartFile avatar);


    void deleteAccount(String token);
}
