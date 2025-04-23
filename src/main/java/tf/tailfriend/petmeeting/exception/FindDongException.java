package tf.tailfriend.petmeeting.exception;

import org.springframework.http.HttpStatus;
import tf.tailfriend.global.exception.CustomException;

import static tf.tailfriend.petmeeting.message.ErrorMessage.FIND_DONG_FAIL;

public class FindDongException extends CustomException {
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getMessage() {
        return FIND_DONG_FAIL.getMessage();
    }
}
