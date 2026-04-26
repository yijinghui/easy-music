package com.easy.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
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
     * OpenAPI 3 配置
     */
    @Bean
    public OpenAPI customOpenAPI() {
        log.info("开始加载 Knife4j OpenAPI 3 文档");

        return new OpenAPI()
                .info(new Info()
                        .title("easy-music系统接口文档")
                        .description("easy-music系统接口文档")
                        .version("1.0")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }

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


    /*@Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate, rolePermissionManager))
                .addPathPatterns("/**").order(0);

        // 登录接口和注册接口不拦截
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/admin/**","/banner/**","/user/**","/playlist/**","/artist/**","/song/**","/comment/**","/feedback/**","/favorite/**") // 拦截所有请求
                .excludePathPatterns(

                        "/admin/login", "/admin/logout", "/admin/register",
                        "/user/login", "/user/logout", "/user/register",
                        "/user/sendVerificationCode", "/user/resetUserPassword",
                        "/banner/getBannerList",
                        "/playlist/getAllPlaylists", "/playlist/getRecommendedPlaylists", "/playlist/getPlaylistDetail/**",
                        "/artist/getAllArtists", "/artist/getArtistDetail/**",
                        "/song/getAllSongs", "/song/getRecommendedSongs", "/song/getSongDetail/**")
                .order(1);

    }*/
}
