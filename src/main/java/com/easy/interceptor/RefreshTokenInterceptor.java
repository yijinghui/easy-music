package com.easy.interceptor;


import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.easy.constant.MessageConstant;
import com.easy.utils.JwtUtil;
import com.easy.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Slf4j
public class RefreshTokenInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;


    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true; // 直接放行，确保 CORS 预检请求不会被拦截
        }

        String token = request.getHeader("Authorization");


        if (token == null){
            return true;
        }

        log.info("token: {}", token);
        if (token.startsWith("Bearer ")){
            token = token.substring(7);
        }

        // 从redis中获取相同的token
        Map<String, Object> claims;

        try{
            claims = JwtUtil.parseToken(token);
        }catch (Exception e) {
            return true;
        }

        if (claims == null){
            return true;
        }

        log.info("claims: {}", claims);
        ThreadLocalUtil.set(claims);

        long userId = Long.parseLong(claims.get("userId").toString());
        String role = claims.get("role").toString();

        stringRedisTemplate.expire("login:"+role+":"+userId, 1, TimeUnit.HOURS);
        return true;


    }


}
