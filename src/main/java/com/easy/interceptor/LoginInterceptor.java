package com.easy.interceptor;


import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.easy.config.RolePermissionManager;
import com.easy.constant.JwtClaimsConstant;
import com.easy.constant.MessageConstant;
import com.easy.enumeration.RoleEnum;
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

    private final RolePermissionManager RolePermissionManager;

    public LoginInterceptor(StringRedisTemplate stringRedisTemplate, RolePermissionManager rolePermissionManager) {
        this.stringRedisTemplate = stringRedisTemplate;
        RolePermissionManager = rolePermissionManager;
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

        if (token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims == null){
            log.info("未登录，返回信息：{}", MessageConstant.NOT_LOGIN);
            sendErrorResponse(response, 401, MessageConstant.NOT_LOGIN);
            return false;
        }

        log.info("claims{}", claims);
        String userId = claims.get(JwtClaimsConstant.USER_ID).toString();
        String role = claims.get(JwtClaimsConstant.ROLE).toString();
        log.info("用户角色为：{}", role);
        String value = stringRedisTemplate.opsForValue().get("login:"+role+":"+userId);
        if (value == null){
            log.info("身份已过期，返回信息：{}", MessageConstant.NOT_LOGIN);
            sendErrorResponse(response, 401, "身份已过期");
            return false;
        }

        if (!token.equals(value)){
            log.info("无效的Token，返回信息：{}", MessageConstant.NOT_LOGIN);
            sendErrorResponse(response, 401, "无效的Token");
            return false;
        }

        // 查看用户是否有权限访问该接口
        String requestURI = request.getRequestURI();
        if (!RolePermissionManager.hasPermission(role, requestURI)){
            log.info("无权限访问该接口，返回信息：{}", MessageConstant.NO_PERMISSION);
            sendErrorResponse(response, 403, "无权限访问该接口");
            return false;
        }


        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清空ThreadLocal中的数据
        ThreadLocalUtil.remove();
    }

}
