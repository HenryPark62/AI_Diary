package backend.User.apiPayload.code.status;

import backend.User.apiPayload.code.BaseErrorCode;
import backend.User.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

  // Common
  _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러"),
  _BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
  _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
  _FORBIDDEN(HttpStatus.FORBIDDEN, "금지된 요청입니다."),

  // Custom Common
  INTERNAL_SERVER_ERROR_CUSTOM(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
  BAD_REQUEST_CUSTOM(HttpStatus.BAD_REQUEST, "요청 형식이 잘못되었습니다."),
  UNAUTHORIZED_CUSTOM(HttpStatus.UNAUTHORIZED, "인가되지 않은 요청입니다."),
  NOT_FOUND_CUSTOM(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),

  // Email
  EMAIL_SEND_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송에 실패했습니다."),
  EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다."),
  EMAIL_PENDING_VERIFICATION(HttpStatus.BAD_REQUEST, "이미 가입 진행 중인 이메일입니다."),

  // S3
  S3_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 업로드 중 오류가 발생했습니다."),

  // Test
  TEMP_EXCEPTION(HttpStatus.BAD_REQUEST, "테스트"),

  // Page
  NOT_PAGE(HttpStatus.BAD_REQUEST, "페이지 요청이 잘못되었습니다. (page = 1 이상의 int)"),

  // ChatRoom
  ROOM_NOT_FOUND(HttpStatus.BAD_REQUEST,"존재하지 않는 채팅방입니다."),
  USER_ALREADY_IN_ROOM(HttpStatus.BAD_REQUEST, "이미 참여 중인 채팅방입니다."),
  INVALID_UUID(HttpStatus.BAD_REQUEST, "유효한 UUID 형식이 아닙니다."),
  USER_NOT_IN_ROOM(HttpStatus.BAD_REQUEST, "참여 중인 방이 아닙니다."),
  NOT_ROOM_OWNER(HttpStatus.FORBIDDEN, "방장 권한입니다."),

  // User
  USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 사용자입니다."),
  NICKNAME_NOT_EXIST(HttpStatus.BAD_REQUEST, "닉네임은 필수입니다."),
  NOT_AUTHENTICATED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
  INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),


  // Token
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
  TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "토큰을 찾을 수 없습니다. (인증이 필요한 서비스)"),
  EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "accessToken이 만료되었습니다.");

  private final HttpStatus httpStatus;
  private final String message;

  @Override
  public ErrorReasonDTO getReason() {
    return ErrorReasonDTO.builder()
        .message(message)
        .status(httpStatus.value())
        .isSuccess(false)
        .build();
  }

  @Override
  public ErrorReasonDTO getReasonHttpStatus() {
    return getReason(); // 이제 중복 없어도 돼!
  }
}