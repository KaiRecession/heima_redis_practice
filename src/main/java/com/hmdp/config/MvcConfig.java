package com.hmdp.config;

import com.hmdp.utils.LoginInterceptor;
import com.hmdp.utils.RefreshInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录拦截器
        registry.addInterceptor(new LoginInterceptor())
                .order(1)
                .excludePathPatterns(
                        "/shop/**",
                        "/shop-type/**",
                        "/upload/**",
                        "/voucher/**",
                        "/blog/hot",
                        "/user/code",
                        "/user/login"
                );
        // 确保这个拦截器是最先执行的，拦截器也是有顺序的
        // token拦截器，默认拦截所有的请求，这样不管是访问哪个网站都能够刷新token的有效期
        registry.addInterceptor(new RefreshInterceptor(stringRedisTemplate))
                .order(0);
    }
}
