package backend.User.config;

import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    // 커스텀 Multipart 메시지 컨버터를 주입받음
    private final MultipartJackson2HttpMessageConverter multipartConverter;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("Authorization", "Content-Type")
                .exposedHeaders("Custom-Header")
                .allowCredentials(true)
                .maxAge(3600);
    }

    // extendMessageConverters를 오버라이드하여 커스텀 컨버터를 등록함
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 커스텀 컨버터를 가장 앞에 추가하여 우선순위를 높임
        converters.add(0, multipartConverter);
    }
}
