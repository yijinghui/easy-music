package com.easy.service;

import com.easy.pojo.vo.SignInVO;
import com.easy.result.Result;

import java.time.LocalDate;

public interface SignInService {
    Result signIn();

    Result repairSignIn(LocalDate targetDate);

    Result<SignInVO> getInfo();
}
