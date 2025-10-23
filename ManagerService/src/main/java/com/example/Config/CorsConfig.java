package Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 所有接口[6,9](@ref)
                .allowedOriginPatterns("*") // 允许所有域名，使用allowedOriginPatterns代替allowedOrigins[9](@ref)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的请求方法[6,10](@ref)
                .allowedHeaders("*") // 允许所有请求头[6,9](@ref)
                .allowCredentials(true) // 允许发送cookie[6,9](@ref)
                .maxAge(3600); // 预检请求的缓存时间（秒）[6,9](@ref)
    }
}