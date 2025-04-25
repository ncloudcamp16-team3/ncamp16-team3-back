package tf.tailfriend.board.exception;

import org.springframework.http.HttpStatus;
import tf.tailfriend.global.exception.CustomException;

import static tf.tailfriend.board.message.ErrorMessage.GET_POST_FAIL;

public class GetPostException extends CustomException {
  @Override
  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public String getMessage() {
    return GET_POST_FAIL.getMessage();
  }
}
