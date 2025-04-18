package tf.tailfriend.petmeeting.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {
    GET_FRIENDS_SUCCESS("친구 조회에 실패하였습니다");
    private final String message;
}
