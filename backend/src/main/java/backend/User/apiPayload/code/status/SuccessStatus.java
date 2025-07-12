package backend.User.apiPayload.code.status;

import backend.User.apiPayload.code.BaseCode;
import backend.User.apiPayload.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseCode {

  _OK(HttpStatus.OK, "성공입니다.");

  private final HttpStatus httpStatus;
  private final String message;

  @Override
  public ReasonDTO getReason() {
    return ReasonDTO.builder()
        .status(httpStatus.value())
        .isSuccess(true)
        .message(message)
        .httpStatus(httpStatus)
        .build();
  }

  @Override
  public ReasonDTO getReasonHttpStatus() {
    return ReasonDTO.builder()
        .status(httpStatus.value())
        .isSuccess(true)
        .message(message)
        .httpStatus(httpStatus)
        .build();
  }
}