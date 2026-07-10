package com.easy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easy.pojo.dto.*;
import com.easy.pojo.entity.User;
import com.easy.pojo.vo.UserVO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService extends IService<User> {
    PageResult list(UserPageQueryDTO userPageQueryDTO);


    void updateStatus(Long userId, Integer userStatus);


    String loginByPassword(UserPasswordLoginDTO userLoginDTO);

    void register(UserRegisterDTO userRegisterDTO);

    void sendVerificationCode(String email,Integer operationType);

    void logout();

    UserVO userInfo(Long userId);

    void updateUserInfo(@Valid UserDTO userDTO);


    void updateUserEmail(@Valid UserEmailUpdateDTO updateDTO);

    void updateUserPassword(UserResetPasswordDTO updateDTO);

    String loginByEmail(@Valid UserEmailLoginDTO userLoginDTO);

    void updateAvatar(Long userId, MultipartFile avatar);


    void deleteAccount(String token);

    PageResult search(@NotBlank(message = "搜索内容不能为空") String username, @Valid PageQueryDTO pageQueryDTO);
}
