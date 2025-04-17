package tf.tailfriend.admin.exception;

import lombok.Getter;

@Getter
public class AdminException extends RuntimeException {

    private final String message;

    public AdminException(String message) {
        super(message);
        this.message = message;
    }
}
