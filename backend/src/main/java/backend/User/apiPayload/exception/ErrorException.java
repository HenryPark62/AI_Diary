package backend.User.apiPayload.exception;

import backend.User.apiPayload.code.BaseErrorCode;

public class ErrorException extends GeneralException {

    public ErrorException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
