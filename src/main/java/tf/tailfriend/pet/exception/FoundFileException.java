package tf.tailfriend.pet.exception;

import org.springframework.http.HttpStatus;
import tf.tailfriend.global.exception.CustomException;

import static tf.tailfriend.pet.message.ErrorMessage.GET_FIlE_FAIL;

public class FoundFileException extends CustomException {
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    public String getMessage() {
        return GET_FIlE_FAIL.getMessage();
    }
}
