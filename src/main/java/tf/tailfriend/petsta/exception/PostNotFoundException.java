package tf.tailfriend.petsta.exception;

import org.springframework.http.HttpStatus;
import tf.tailfriend.global.exception.CustomException;

public class PostNotFoundException extends CustomException {
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getMessage() {
        return "해당 게시글을 찾을 수 없거나 삭제되었습니다.";
    }}
