package tf.tailfriend.board.exception;

import org.springframework.http.HttpStatus;
import tf.tailfriend.global.exception.CustomException;

import static tf.tailfriend.board.message.ErrorMessage.GET_ANNOUNCE_FAIL;

public class GetAnnounceException extends CustomException {
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    public String getMessage() {
        return GET_ANNOUNCE_FAIL.getMessage();
    }
}
