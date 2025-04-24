package tf.tailfriend.user.exception;

import org.springframework.http.HttpStatus;
import tf.tailfriend.global.exception.CustomException;

import static tf.tailfriend.user.message.error.ErrorMessage.DISTANCE_CODE_NOT_FOUND;

public class DistanceCodeException extends CustomException {
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    public String getMessage() {
        return DISTANCE_CODE_NOT_FOUND.getMessage();
    }
}
