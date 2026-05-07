package com.easy.interceptor;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

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
            return true; // 令牌不存在，直接放行
        }

        log.info("token: {}", token);
        if (token.startsWith("Bearer ")){
            token = token.substring(7);
        }

        // 从redis中获取相同的token
        String value = stringRedisTemplate.opsForValue().get(token);
        if (value == null){
            return true;
        }

        stringRedisTemplate.expire(token, 1, TimeUnit.HOURS);

        return true;


    }


}
