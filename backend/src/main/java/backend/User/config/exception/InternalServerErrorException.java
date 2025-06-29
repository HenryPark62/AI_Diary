package backend.User.config.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class InternalServerErrorException extends RuntimeException {

    // 단일 String 인자를 받는 기존 생성자
    public InternalServerErrorException(String message) {
        super(message);
    }

    // 메시지와 원인 예외(Throwable)를 함께 받는 생성자 추가
    public InternalServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
