package tf.tailfriend.user.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import tf.tailfriend.global.exception.CustomException;
import tf.tailfriend.user.message.ErrorMessage;

@Getter
public class SnsTypeException extends CustomException {

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getMessage() {
        return ErrorMessage.SNSTYPE_NOTFOUND.getMessage();
    }
}
