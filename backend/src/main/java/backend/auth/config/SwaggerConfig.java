package backend.auth.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(title = "AI_DIARY", version = "v1"),
        servers = @Server(url = "/", description = "서버 URL"),
        security = {
                @SecurityRequirement(name = "Language_cookieAuth"),
                @SecurityRequirement(name = "JWT_cookieAuth"),
                @SecurityRequirement(name = "bearerAuth")
        }
)


@SecurityScheme(
        name = "bearerAuth", // 보안 스키마 이름 설정
        type = SecuritySchemeType.HTTP, // HTTP 스키마 유형 설정
        scheme = "bearer", // 인증 방식 설정
        bearerFormat = "JWT" // 베어러 형식 설정 (선택 사항)
)

@SecurityScheme(
        name = "Language_cookieAuth", // 쿠키 인증용 스키마
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.COOKIE,  // 쿠키에 전달됨을 명시
        paramName = "language"
)

@SecurityScheme(
        name = "JWT_cookieAuth", // 쿠키 인증용 스키마
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.COOKIE,  // 쿠키에 전달됨을 명시
        paramName = "jwt"
)

@RequiredArgsConstructor
@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi SwaggerOpenApi() {
        return GroupedOpenApi.builder()
                .group("AI_DIARY API")
                .pathsToMatch("/**")
                .build();
    }
}