package com.easy.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.constant.JwtClaimsConstant;
import com.easy.enumeration.RoleEnum;
import com.easy.mapper.AdminMapper;
import com.easy.pojo.dto.AdminDTO;
import com.easy.pojo.dto.AdminUpdatePasswordDTO;
import com.easy.pojo.entity.Admin;
import com.easy.result.Result;
import com.easy.service.AdminService;
import com.easy.utils.JwtUtil;
import com.easy.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    private final AdminMapper adminMapper;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Result login(AdminDTO adminDTO) {
        // 1.查询用户信息
        Admin admin = adminMapper.selectOne(new QueryWrapper<Admin>().eq("username", adminDTO.getUsername()));
        if (admin == null) {
            return Result.error("用户不存在");
        }

        // 2.使用MD5对密码进行加密
        String passwordMD5 = DigestUtils.md5DigestAsHex(adminDTO.getPassword().getBytes());

        // 3.比较密码
        if (!passwordMD5.equals(admin.getPassword())) {
            return Result.error("密码错误");
        }

        // 4.登录成功
        log.info("用户{}，登录成功！",adminDTO);
        // 4.1.生成Jwt令牌
        Long adminId = admin.getAdminId();
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.ROLE, RoleEnum.ADMIN.getRole());
        claims.put(JwtClaimsConstant.USER_ID, adminId);
        claims.put(JwtClaimsConstant.USERNAME, admin.getUsername());
        String token = JwtUtil.generateToken(claims);
        // 4.2.将token存入Redis
        stringRedisTemplate.opsForValue().set("login:"+RoleEnum.ADMIN.getRole()+":" + adminId, token, 10, TimeUnit.MINUTES);

        // 5.返回结果
        return Result.success("登录成功", token);
    }


    @Override
    public Result logout(String token) {
        Long adminId = ThreadLocalUtil.getUserId();
        String tokenValue = stringRedisTemplate.opsForValue().get("login:"+RoleEnum.ADMIN.getRole()+":" + adminId);
        // 1.检查token是否存在
        if (tokenValue == null) return Result.error("登录已过期，请重新登录");
        // 2.检查token是否有效
        if (!tokenValue.equals("1")) return Result.error("登录异常，请重新登录");

        // 3.注销token
        Boolean result = stringRedisTemplate.delete("login:"+RoleEnum.ADMIN.getRole()+":" + adminId);
        return result ? Result.success("注销成功！") : Result.error("注销失败！");
    }

    @Override
    public Result updatePassword(AdminUpdatePasswordDTO updatePasswordDTO) {
        Admin admin =new Admin();
        BeanUtil.copyProperties(updatePasswordDTO,admin);
        return adminMapper.updateById(admin) > 0 ? Result.success("修改密码成功！") : Result.error("修改密码失败！");
    }


}
