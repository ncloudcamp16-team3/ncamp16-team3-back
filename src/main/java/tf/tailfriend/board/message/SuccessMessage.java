package tf.tailfriend.board.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessMessage {
    GET_POST_SUCCESS("게시물 조회에 성공하였습니다"),
    GET_BOARD_TYPE_SUCCESS("게시판 리스트 조회에 성공하였습니다"),
    SEARCH_POST_SUCCESS("게시물 검색에 성공하였습니다"),
    GET_ANNOUNCE_SUCCESS("공지목록 조회에 성공하였습니다"),
    GET_ANNOUNCE_DETAIL_SUCCESS("공지상세 조회에 성공하였습니다"),
    GET_BOARD_STATUS_SUCCESS("북마크와 좋아요 정보 조회에 성공하였습니다");

    private final String message;
}
