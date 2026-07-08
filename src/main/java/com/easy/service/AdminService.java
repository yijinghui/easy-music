package com.easy.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.easy.pojo.dto.AdminDTO;
import com.easy.pojo.entity.Admin;
import jakarta.validation.Valid;

public interface AdminService extends IService<Admin> {
    String login(@Valid AdminDTO adminDTO);

    // Result register(@Valid AdminDTO adminDTO);

    void logout(String token);

    void updatePassword(String newPassword);
}
