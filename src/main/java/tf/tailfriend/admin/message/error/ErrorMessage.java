package tf.tailfriend.admin.message.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {
    EMAIL_NOTFOUND("이메일을 확인해주세요"),
    PASSWORD_WRONG("비밀번호를 확인해주세요");

    private final String message;
}
