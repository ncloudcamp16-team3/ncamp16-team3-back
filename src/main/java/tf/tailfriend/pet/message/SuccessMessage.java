package tf.tailfriend.pet.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessMessage {
    PET_FOUND_SUCCESS("반려동물 정보 조회에 성공하였습니다");

    private final String message;
}
