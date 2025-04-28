package tf.tailfriend.pet.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {
    PET_FOUND_ERROR("반려동물 정보 조회 중 오류가 발생했습니다"),
    GET_FIlE_FAIL("파일 조회에 실패하였습니다"),
    FIND_DONG_FAIL("잘못된 동 주소입니다"),
    ACTIVITY_STATUS_NONE_UNAVAILABLE("휴식 중인 친구들 정보는 제공되지 않습니다"),
    PET_UPDATE_ERROR("반려동물 수정중 오류가 발생했습니다"),
    GET_MY_PETS_FAIL("내 애완동물 가져오기에 실패하였습니다");

    private final String message;
}
