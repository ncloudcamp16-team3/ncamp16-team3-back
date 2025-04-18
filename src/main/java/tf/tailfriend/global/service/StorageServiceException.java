package tf.tailfriend.global.service;


public class StorageServiceException extends Throwable {
    public StorageServiceException(String message) {
        super(message);
    }

    public StorageServiceException(Throwable cause) {
        super(cause);
    }

    public StorageServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
