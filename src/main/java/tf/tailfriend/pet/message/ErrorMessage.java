package tf.tailfriend.pet.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {
    PET_FOUND_ERROR("반려동물 정보 조회 중 오류가 발생했습니다");

    private final String message;
}
