package tf.tailfriend.user.exception;

import org.springframework.http.HttpStatus;
import tf.tailfriend.global.exception.CustomException;

import static tf.tailfriend.user.message.ErrorMessage.UNAUTHORIZED_ACCESS_ERROR;

public class UnauthorizedException extends CustomException {
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNAUTHORIZED;
    }

    @Override
    public String getMessage() {
        return UNAUTHORIZED_ACCESS_ERROR.getMessage();
    }
}
