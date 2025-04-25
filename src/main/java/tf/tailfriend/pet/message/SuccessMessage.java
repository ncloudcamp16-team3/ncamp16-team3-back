package tf.tailfriend.pet.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessMessage {
    PET_FOUND_SUCCESS("반려동물 정보 조회에 성공하였습니다"),
    GET_FRIENDS_SUCCESS("친구 조회에 성공하였습니다"),
    GET_MY_PETS_SUCCESS("내 반려동물 조회에 성공하였습니다"),
    UPDATE_PET_SUCCESS("반려동물 정보 수정에 성공하였습니다");

    private final String message;
}
