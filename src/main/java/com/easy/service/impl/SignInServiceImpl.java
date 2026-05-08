package com.easy.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.mapper.SignInMapper;
import com.easy.pojo.entity.SignIn;
import com.easy.pojo.vo.SignInVO;
import com.easy.result.Result;
import com.easy.service.SignInService;
import com.easy.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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
        String key="sign:user:"+userId+now.format(DateTimeFormatter.ofPattern(":yyyyMM"));

        // 获取当前月的当前天数
        int day = now.getDayOfMonth();

        stringRedisTemplate.opsForValue().setBit(key,day-1,true);

        // 插入签到记录到数据库
        SignIn signIn = new SignIn();
        signIn.setUserId(userId);
        signIn.setSignTime(now);
        signIn.setSignDay(now.toLocalDate());

        baseMapper.insert(signIn);

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


        String key="sign:user:"+userId+now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String repairKey="signRepair:user:"+userId+now.format(DateTimeFormatter.ofPattern(":yyyyMM"));

        SignInVO signInVO = new SignInVO();

        Integer totalSignInCount = 0;

        // 获取签到表
        // 1.获取当前月的天数
        int days = now.lengthOfMonth();
        // 2.创建List集合
        List<Boolean> signInTable = new ArrayList<>();
        // 3.获取数据
        for (int i = 0; i < days; i++) {
            Boolean bit = stringRedisTemplate.opsForValue().getBit(key, i);
            if (Boolean.FALSE.equals(bit)){
                signInTable.add(false);
            }else {
                signInTable.add(true);
                totalSignInCount++;
            }
        }


        signInVO.setSignInTable(signInTable);
        signInVO.setSignInCount(totalSignInCount);

        // 获取剩余补签次数
        String repairCount = stringRedisTemplate.opsForValue().get(repairKey);
        if (repairCount==null){
            repairCount="5";
            stringRedisTemplate.opsForValue().set(repairKey,repairCount);
        }
        signInVO.setSignInRepairCount(Integer.parseInt(repairCount));

        return Result.success(signInVO);

    }
}
