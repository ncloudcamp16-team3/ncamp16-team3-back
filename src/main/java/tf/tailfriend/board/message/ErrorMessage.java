package tf.tailfriend.board.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {
    GET_POST_FAIL("게시물 조회에 실패하였습니다"),
    GET_BOARD_TYPE_FAIL("게시판 리스트 조회에 실패하였습니다"),
    SEARCH_POST_FAIL("게시물 검색에 실패하였습니다"),
    GET_ANNOUNCE_FAIL("공지목록 조회에 실패하였습니다"),
    GET_ANNOUNCE_DETAIL_FAIL("공지상세 조회에 실패하였습니다"),
    GET_BOARD_STATUS_FAIL("북마크와 좋아요 정보 조회에 실패하였습니다");

    private final String message;
}
