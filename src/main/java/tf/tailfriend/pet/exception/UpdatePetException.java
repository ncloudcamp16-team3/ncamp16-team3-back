package tf.tailfriend.pet.exception;

import org.springframework.http.HttpStatus;
import tf.tailfriend.global.exception.CustomException;

import static tf.tailfriend.pet.message.ErrorMessage.PET_UPDATE_ERROR;

public class UpdatePetException extends CustomException {
    @Override
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    public String getMessage() {
        return PET_UPDATE_ERROR.getMessage();
    }
}
