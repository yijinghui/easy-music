package com.easy.service;


import com.easy.pojo.dto.AdminDTO;
import com.easy.result.Result;
import jakarta.validation.Valid;

public interface AdminService {
    Result login(@Valid AdminDTO adminDTO);

    // Result register(@Valid AdminDTO adminDTO);

    Result logout(String token);

}
