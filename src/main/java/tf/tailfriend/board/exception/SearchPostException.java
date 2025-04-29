package tf.tailfriend.board.exception;

import org.springframework.http.HttpStatus;
import tf.tailfriend.global.exception.CustomException;

import static tf.tailfriend.board.message.ErrorMessage.SEARCH_POST_FAIL;

public class SearchPostException extends CustomException {
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getMessage() {
        return SEARCH_POST_FAIL.getMessage();
    }
}
