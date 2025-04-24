package tf.tailfriend.user.exception;

import org.springframework.http.HttpStatus;
import tf.tailfriend.global.exception.CustomException;

import static tf.tailfriend.user.message.ErrorMessage.USER_SAVE_FAIL;

public class UserSaveException extends CustomException {

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    public String getMessage() {
        return USER_SAVE_FAIL.getMessage();
    }
}
