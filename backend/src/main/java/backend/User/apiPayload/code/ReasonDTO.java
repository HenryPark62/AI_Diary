package backend.User.apiPayload.code;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class ReasonDTO {

  private HttpStatus httpStatus;

  private final boolean isSuccess;
  private final int status;
  private final String message;

  public boolean getIsSuccess() { return isSuccess; }
}
