package com.easy.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.constant.JwtClaimsConstant;
import com.easy.enumeration.RoleEnum;
import com.easy.exception.BaseException;
import com.easy.mapper.AdminMapper;
import com.easy.pojo.dto.AdminDTO;
import com.easy.pojo.entity.Admin;
import com.easy.service.AdminService;
import com.easy.utils.JwtUtil;
import com.easy.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public String login(AdminDTO adminDTO) {
        // 1.查询用户信息
        Admin admin = lambdaQuery()
                .eq(Admin::getUsername, adminDTO.getUsername())
                .one();
        if (admin == null) {
            throw new BaseException("账号不存在");
        }
        // 2.使用MD5对密码进行加密
        String passwordMD5 = DigestUtils.md5DigestAsHex(adminDTO.getPassword().getBytes());

        // 3.比较密码是否匹配
        if (!passwordMD5.equals(admin.getPassword())) {
            throw new BaseException("密码错误");
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
        return token;
    }


    @Override
    public void logout(String token) {
        Long adminId = ThreadLocalUtil.getUserId();
        // 3.注销token
        stringRedisTemplate.delete("login:"+RoleEnum.ADMIN.getRole()+":" + adminId);
    }

    @Override
    public void updatePassword(String newPassword) {
        Long adminId = ThreadLocalUtil.getUserId();
        Admin admin = getById(adminId);
        if(admin.getPassword().equals(newPassword)){
            throw new BaseException("新密码不能与旧密码相同");
        }
        Admin updateAdmin = new Admin();
        updateAdmin.setAdminId(adminId);
        updateAdmin.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        updateById(updateAdmin);
    }


}
