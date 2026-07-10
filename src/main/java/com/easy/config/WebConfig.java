package com.easy.config;


import com.easy.interceptor.LoginInterceptor;
import com.easy.interceptor.RefreshTokenInterceptor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RolePermissionManager rolePermissionManager;

    /**
     * Knife4j 静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始加载 Knife4j 静态资源");

        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/4.15.5/");
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate))
                .addPathPatterns("/**").order(0);

        // 登录接口和注册接口不拦截
        registry.addInterceptor(new LoginInterceptor(stringRedisTemplate,rolePermissionManager))
                .addPathPatterns("/admin/**","/playlist/**","/artist/**","/song/**","/comment/**","/favorite/**","/room/**","/user/**") // 拦截所有请求
                .excludePathPatterns(
                        // 不拦截的请求路径（未登录也可访问）
                        "/admin/login", "/admin/logout",
                        "/banner/*",
                        "/user/login/email","/user/login/password", "/user/register","/user/search","/user/info/{userId}",
                        "/user/code/**","/user/password/reset",
                        "/song/recommend", "/song/top200/week","/song/top200/month","/song/search",
                        "/playlist/search","/playlist/{playlistId}"
                )
                .order(1);



    }
}
