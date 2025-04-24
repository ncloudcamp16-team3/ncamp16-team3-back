package tf.tailfriend.petmeeting.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {
    GET_FRIENDS_FAIL("친구 조회에 실패하였습니다"),
    GET_FIlE_FAIL("파일 조회에 실패하였습니다"),
    FIND_DONG_FAIL("잘못된 동 주소입니다"),
    ACTIVITY_STATUS_NONE_UNAVAILABLE("휴식 중인 친구들 정보는 제공되지 않습니다");
    private final String message;
}
