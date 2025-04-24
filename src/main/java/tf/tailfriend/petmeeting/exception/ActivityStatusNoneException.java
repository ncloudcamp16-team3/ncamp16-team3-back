package tf.tailfriend.petmeeting.exception;

import org.springframework.http.HttpStatus;
import tf.tailfriend.global.exception.CustomException;

import static tf.tailfriend.petmeeting.message.ErrorMessage.ACTIVITY_STATUS_NONE_UNAVAILABLE;

public class ActivityStatusNoneException extends CustomException {
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getMessage() {
        return ACTIVITY_STATUS_NONE_UNAVAILABLE.getMessage();
    }
}
