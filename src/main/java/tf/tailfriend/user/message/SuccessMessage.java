package tf.tailfriend.user.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessMessage {
    USER_INFO_SAVE_SUCCESS("유저정보 저장에 성공하였습니다"),
    USER_REGISTER_SUCCESS("회원가입 및 로그인 성공"),
    LOGOUT_SUCCESS("로그아웃 완료"),
    USER_INFO_FETCH_SUCCESS("유저 정보 조회 성공"),
    CHECK_LOGIN_SUCCESS("로그인 확인 성공"),
    CHECK_NICKNAME_SUCCESS("닉네임 중복 확인 성공"),
    CHECK_NOTIFICATION_READ_SUCCESS("알람 읽음여부 확인 성공");

    private final String message;
}
