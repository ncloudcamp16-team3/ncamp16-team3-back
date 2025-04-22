package tf.tailfriend.admin.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import tf.tailfriend.admin.message.error.ErrorMessage;
import tf.tailfriend.global.exception.CustomException;

@Getter
public class ExistEmailException extends CustomException {

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getMessage() {
        return ErrorMessage.EXIST_EMAIL.getMessage();
    }
}
