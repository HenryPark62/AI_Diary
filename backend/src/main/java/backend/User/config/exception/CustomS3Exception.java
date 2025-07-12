package backend.User.config.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class CustomS3Exception extends RuntimeException {
    public CustomS3Exception(String message) {
        super(message);
    }

    public CustomS3Exception(String message, Throwable cause) {
        super(message, cause);
    }
}
