package backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private String code; // 에러 코드 필드 추가
    private T data;

    public static <T> ApiResponse<T> onSuccess(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("success")
                .code(null)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> onFailure(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .code(null)
                .data(null)
                .build();
    }

    public static <T> ApiResponse<T> onFailure(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .code(code)
                .data(null)
                .build();
    }
}