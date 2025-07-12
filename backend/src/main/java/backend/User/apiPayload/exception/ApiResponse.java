package backend.User.apiPayload.exception;

import backend.User.apiPayload.code.BaseCode;
import backend.User.apiPayload.code.status.SuccessStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class ApiResponse<T> {

    @JsonProperty("isSuccess")
    private final Boolean isSuccess;
    private final int status;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;

    // 성공한 경우 응답 생성
    public static <T> ApiResponse<T> onSuccess(T result) {
        return new ApiResponse<>(true, SuccessStatus._OK.getHttpStatus().value(), SuccessStatus._OK.getMessage(),
                result);
    }

    public static <T> ApiResponse<T> of(BaseCode code, T result) {
        return new ApiResponse<>(true, code.getReasonHttpStatus().getStatus(), code.getReasonHttpStatus().getMessage(),
                result);
    }

    //실패한 경우 응답 생성
    public static <T> ApiResponse<T> onFailure(int status, String message, T data) {
        return new ApiResponse<>(false, status, message, data);
    }
}
