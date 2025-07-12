package backend.User.apiPayload.exception;

import backend.User.apiPayload.code.BaseErrorCode;

public class TempException extends GeneralException {

    public TempException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
