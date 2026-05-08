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
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;

    public LoginInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }



    public void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8"); // 设置字符编码为UTF-8
        response.setContentType("application/json;charset=UTF-8"); // 设置响应的Content-Type
        response.getWriter().write(message);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true; // 直接放行，确保 CORS 预检请求不会被拦截
        }

        String path = request.getRequestURI();
        log.info("用户访问的接口路径为：{}", path);


        // 验证用户是否登录
        String token = request.getHeader("Authorization");

        if (token == null){
            log.info("用户未登录，返回信息：{}", MessageConstant.NOT_LOGIN);
            sendErrorResponse(response, 401, MessageConstant.NOT_LOGIN);
            return false;
        }

        log.info("token: {}", token);
        if (token.startsWith("Bearer ")){
            token = token.substring(7);
        }

        // 从redis中获取相同的token
        Map<String, Object> claims;

        try{
            claims = JwtUtil.parseToken(token);
        }catch (TokenExpiredException e) {
            // token过期
            sendErrorResponse(response, 401, "Token已过期，请重新登录");
            return false;
        } catch (JWTVerificationException e) {
            // token无效
            sendErrorResponse(response, 401, "无效的Token");
            return false;
        }

        if (claims == null){
            log.info("用户未登录，返回信息：{}", "token不合法");
            sendErrorResponse(response, 401, "无效的Token");
            return false;
        }
        long userId = Long.parseLong(claims.get("userId").toString());

        String value = stringRedisTemplate.opsForValue().get("login:user:"+userId);
        if (value == null){
            log.info("身份已过期，返回信息：{}", MessageConstant.NOT_LOGIN);
            sendErrorResponse(response, 401, "身份已过期");
            return false;
        }

        ThreadLocalUtil.set(claims);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清空ThreadLocal中的数据
        ThreadLocalUtil.remove();
    }

}
