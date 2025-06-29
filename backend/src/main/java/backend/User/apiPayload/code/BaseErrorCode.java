package backend.User.apiPayload.code;

import backend.User.apiPayload.code.ErrorReasonDTO;

public interface BaseErrorCode {

  ErrorReasonDTO getReason();

  ErrorReasonDTO getReasonHttpStatus();
}
