package com.easy.service;

import com.easy.pojo.vo.UserStatVO;
import com.easy.result.Result;

import java.time.LocalDate;
import java.util.Map;

public interface StatisticService {
    UserStatVO getUserStat(Long userId);

    Result<Map<String, Object>> getUserStatistic(Integer days);

    Result<Map<String, Object>> getSongPlayData(Integer days);
}
