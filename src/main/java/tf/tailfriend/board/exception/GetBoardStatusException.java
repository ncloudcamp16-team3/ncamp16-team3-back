package tf.tailfriend.board.exception;

import org.springframework.http.HttpStatus;
import tf.tailfriend.global.exception.CustomException;

import static tf.tailfriend.board.message.ErrorMessage.GET_BOARD_STATUS_FAIL;

public class GetBoardStatusException extends CustomException {
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getMessage() {
        return GET_BOARD_STATUS_FAIL.getMessage();
    }
}
