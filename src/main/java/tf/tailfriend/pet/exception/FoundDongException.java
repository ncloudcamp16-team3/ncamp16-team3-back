package tf.tailfriend.pet.exception;

import org.springframework.http.HttpStatus;
import tf.tailfriend.global.exception.CustomException;

import static tf.tailfriend.pet.message.ErrorMessage.FIND_DONG_FAIL;

public class FoundDongException extends CustomException {
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getMessage() {
        return FIND_DONG_FAIL.getMessage();
    }
}
