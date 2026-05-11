package com.easy.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.mapper.SignInMapper;
import com.easy.pojo.entity.SignIn;
import com.easy.pojo.vo.SignInVO;
import com.easy.result.Result;
import com.easy.service.SignInService;
import com.easy.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SignInServiceImpl extends ServiceImpl<SignInMapper, SignIn> implements SignInService {

    private final StringRedisTemplate stringRedisTemplate;


    @Override
    public Result signIn() {
        Long userId = ThreadLocalUtil.getUserId();
        LocalDateTime now = LocalDateTime.now();
        String key = "sign:user:" + userId + now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        int day = now.getDayOfMonth();

        // 1. 先检查今天是否已签到
        Boolean hasSigned = stringRedisTemplate.opsForValue().getBit(key, day - 1);
        if (Boolean.TRUE.equals(hasSigned)) {
            return Result.error("今日已签到");
        }

        stringRedisTemplate.opsForValue().setBit(key, day - 1, true);

        try {
            SignIn signIn = new SignIn();
            signIn.setUserId(userId);
            signIn.setSignTime(now);
            signIn.setSignDay(now.toLocalDate());
            baseMapper.insert(signIn);
        } catch (Exception e) {
            // 回滚Redis的bit（删除或置为false）
            stringRedisTemplate.opsForValue().setBit(key, day - 1, false);
            return Result.error("签到失败，请稍后重试");
        }

        return Result.success("签到成功");
    }

    @Override
    public Result repairSignIn(LocalDate targetDate) {

        Long userId = ThreadLocalUtil.getUserId();

        if (targetDate==null){
            return Result.error("请选择要补签的日期");
        }

        if (targetDate.isAfter(LocalDate.now())){
            return Result.error("请选择正确的日期");
        }

        LocalDateTime now = LocalDateTime.now();

        // 检查是否在7天内
        if (targetDate.isBefore(now.toLocalDate().minusDays(7))){
            return Result.error("只能补签近7的记录");
        }

        String repairKey="signRepair:user:"+userId+targetDate.format(DateTimeFormatter.ofPattern(":yyyyMM"));

        String key="sign:user:"+userId+targetDate.format(DateTimeFormatter.ofPattern(":yyyyMM"));

        String repairCount = stringRedisTemplate.opsForValue().get(repairKey);

        if (repairCount==null){
            return Result.error("补签异常");
        }

        if (Integer.parseInt(repairCount)<=0){
            return Result.error("补签次数已用尽");
        }



        // 检查是否已经签到
        Boolean result = stringRedisTemplate.opsForValue().getBit(key, targetDate.getDayOfMonth() - 1);
        if (Boolean.TRUE.equals(result)){
            return Result.error("该日期已经签到");
        }

        stringRedisTemplate.opsForValue().decrement(repairKey);
        stringRedisTemplate.opsForValue().setBit(key,targetDate.getDayOfMonth()-1,true);

        SignIn signIn = new SignIn();
        signIn.setUserId(userId);
        signIn.setSignTime(LocalDateTime.now());
        signIn.setSignDay(targetDate);
        baseMapper.insert(signIn);

        return Result.success("补签成功");
    }

    @Override
    public Result<SignInVO> getInfo() {
        Long userId = ThreadLocalUtil.getUserId();
        LocalDate now = LocalDate.now();
        String key = "sign:user:" + userId + now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String repairKey = "signRepair:user:" + userId + now.format(DateTimeFormatter.ofPattern(":yyyyMM"));

        int days = now.lengthOfMonth();

        // 1. 一次 Redis 请求获取所有签到位（Lua脚本或BITFIELD）
        // BITFIELD key GET u31 0  → 获取31位无符号整数
        List<Long> result = stringRedisTemplate.opsForValue().bitField(key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(days))
                        .valueAt(0)
        );

        long bitField = (result != null && !result.isEmpty()) ? result.get(0) : 0L;

        // 2. 解析位图到 List<Boolean>
        List<Boolean> signInTable = new ArrayList<>(days);
        int totalSignInCount = 0;
        for (int i = 0; i < days; i++) {
            boolean signed = ((bitField >> i) & 1) == 1;
            signInTable.add(signed);
            if (signed) totalSignInCount++;
        }

        // 3. 获取补签次数
        String repairCount = stringRedisTemplate.opsForValue().get(repairKey);
        if (repairCount == null) {
            repairCount = Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(repairKey, "5")) ? "5" : stringRedisTemplate.opsForValue().get(repairKey);
        }
        repairCount = repairCount == null ? "0" : repairCount;

        SignInVO signInVO = new SignInVO();
        signInVO.setSignInTable(signInTable);
        signInVO.setSignInCount(totalSignInCount);
        signInVO.setSignInRepairCount(Integer.parseInt(repairCount));

        return Result.success(signInVO);
    }
}
