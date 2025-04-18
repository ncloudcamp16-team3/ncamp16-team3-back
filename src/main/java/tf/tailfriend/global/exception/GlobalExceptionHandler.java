package tf.tailfriend.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tf.tailfriend.global.response.CustomResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> globalExceptionHandler(CustomException e) {
        log.warn("{} Occurred: {}", e.getClass().getSimpleName(), e.getMessage());

        return ResponseEntity
                .status(e.getStatus())
                .body(new CustomResponse(e.getMessage(), null));
    }
}