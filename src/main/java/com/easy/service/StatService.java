package com.easy.service;

import com.easy.pojo.vo.UserStatVO;

public interface StatService {
    UserStatVO getUserStat(Long userId);
}
