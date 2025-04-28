package tf.tailfriend.user.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {
    USER_NOTFOUND("존재하지 않는 계정입니다"),
    SNSTYPE_NOTFOUND("존재하지 않는 SNS 타입입니다"),
    PETTYPE_NOTFOUND("존재하지 않는 PET 타입입니다"),
    DISTANCE_CODE_NOT_FOUND("존재하지 않는 거리 코드입니다"),
    USER_SAVE_FAIL("유저정보 저장에 실패하였습니다"),
    UNAUTHORIZED_ACCESS_ERROR("인증되지 않은 접근입니다");

    private final String message;
}
