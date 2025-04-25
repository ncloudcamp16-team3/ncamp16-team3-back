package tf.tailfriend.board.exception;

import org.springframework.http.HttpStatus;
import tf.tailfriend.global.exception.CustomException;

import static tf.tailfriend.board.message.SuccessMessage.GET_ANNOUNCE_DETAIL_SUCCESS;

public class GetAnnounceDetailException extends CustomException {
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getMessage() {
        return GET_ANNOUNCE_DETAIL_SUCCESS.getMessage();
    }
}
