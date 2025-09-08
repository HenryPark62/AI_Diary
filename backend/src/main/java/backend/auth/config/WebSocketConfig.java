package backend.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 엔드포인트 등록, SockJS fallback 활성화
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
//                .setAllowedOriginPatterns("http://localhost:63342", "http://localhost:5173", "http://54.206.71.88:9090/")
                .addInterceptors(new CustomHandshakeInterceptor()) // 인증 정보를 attributes에 저장
                .setHandshakeHandler(new CustomHandshakeHandler())    // 저장된 principal을 반환하도록 함
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker( "/topic");    // 구독 경로
//        registry.setApplicationDestinationPrefixes("/pub");   // 발행 경로\    registry.setApplicationDestinationPrefixes("/app");   // ← changed from "/pub" to "/app"
        registry.setApplicationDestinationPrefixes("/app");   // ← changed from "/pub" to "/app"
        registry.setUserDestinationPrefix("/user");           // 사용자 대상 경로
    }
}