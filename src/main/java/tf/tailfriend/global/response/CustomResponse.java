package tf.tailfriend.global.response;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CustomResponse {
    private String message;
    private Object data;
}