package tf.tailfriend.petmeeting.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessMessage {
    GET_FRIENDS_SUCCESS("친구 조회에 성공하였습니다");

    private final String message;
}
