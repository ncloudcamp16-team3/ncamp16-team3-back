package tf.tailfriend.board.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.board.dto.BoardRequestDto;
import tf.tailfriend.board.dto.BoardResponseDto;
import tf.tailfriend.board.dto.CommentRequestDto;
import tf.tailfriend.board.dto.SearchRequestDto;
import tf.tailfriend.board.entity.Board;
import tf.tailfriend.board.entity.Comment;
import tf.tailfriend.board.exception.GetBoardStatusException;
import tf.tailfriend.board.exception.GetBoardTypeException;
import tf.tailfriend.board.exception.GetPostException;
import tf.tailfriend.board.exception.SearchPostException;
import tf.tailfriend.board.repository.BoardDao;
import tf.tailfriend.board.service.BoardService;
import tf.tailfriend.board.service.BoardTypeService;
import tf.tailfriend.board.service.CommentService;
import tf.tailfriend.global.config.UserPrincipal;
import tf.tailfriend.global.exception.CustomException;
import tf.tailfriend.global.response.CustomResponse;
import tf.tailfriend.global.service.StorageServiceException;
import tf.tailfriend.notification.scheduler.NotificationScheduler;
import tf.tailfriend.notification.service.NotificationService;
import tf.tailfriend.user.exception.UnauthorizedException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static tf.tailfriend.board.message.SuccessMessage.*;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
@Slf4j
public class BoardController {

    private final BoardService boardService;
    private final BoardTypeService boardTypeService;
    private final CommentService commentService;
    private final NotificationScheduler notificationScheduler;
    private final BoardDao boardDao;
    private final NotificationService notificationService;

    @PostMapping("")
    public ResponseEntity<?> saveBoard(@RequestPart("postData") BoardRequestDto boardRequestDto,
                                       @RequestPart(value = "photos", required = false) List<MultipartFile> photos,
                                       @AuthenticationPrincipal UserPrincipal userPrincipal) throws StorageServiceException {
        log.info("요청 boardRequestDto: {} \nphotos: {}", boardRequestDto, photos);

        try {
            Integer postId = boardService.saveBoard(boardRequestDto, photos, userPrincipal.getUserId());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CustomResponse("게시물 저장에 성공하였습니다", postId));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CustomException() {
                @Override
                public HttpStatus getStatus() {
                    return HttpStatus.BAD_REQUEST;
                }

                @Override
                public String getMessage() {
                    return e.getMessage();
                }
            };
        }
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteBoard(@RequestParam("postId") Integer postId,
                                         @AuthenticationPrincipal UserPrincipal userPrincipal) throws StorageServiceException {
        log.info("삭제요청 요청 commentId: {}", postId);

        try {
            boardService.deleteBoard(postId, userPrincipal.getUserId());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CustomResponse("게시물 삭제에 성공하였습니다", null));
        } catch (Exception e) {
            throw new CustomException() {
                @Override
                public HttpStatus getStatus() {
                    return HttpStatus.BAD_REQUEST;
                }

                @Override
                public String getMessage() {
                    return "게시물 삭제에 실패하였습니다";
                }
            };
        }
    }


    @GetMapping("/detail/{postId}")
    public ResponseEntity<?> getBoardDetail(@PathVariable Integer postId) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CustomResponse(GET_POST_SUCCESS.getMessage(), boardService.getBoardById(postId)));
        } catch (Exception e) {
            throw new GetPostException();
        }
    }

    @PostMapping("/product/complete")
    public ResponseEntity<?> completeSell(@RequestBody Map<String, Integer> body,
                                          @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Integer postId = body.get("postId");
        try {
            boardService.setCompleteSell(postId, userPrincipal.getUserId());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CustomResponse("판매완료 변경에서 성공하였습니다", null));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException() {
                @Override
                public HttpStatus getStatus() {
                    return HttpStatus.BAD_REQUEST;
                }

                @Override
                public String getMessage() {
                    return e.getMessage();
                }
            };
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getBookmarkedAndLiked(@RequestParam("userId") Integer userId,
                                                   @RequestParam("boardId") Integer boardId) {
        log.info("\n북마크 좋아요 상태 요청\n userId: {}, boardId: {}", userId, boardId);

        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CustomResponse(GET_BOARD_STATUS_SUCCESS.getMessage(), boardService.getBoardStatus( userId, boardId)));
        } catch (Exception e) {
            throw new GetBoardStatusException();
        }
    }

    @GetMapping("/bookmark/add")
    public ResponseEntity<?> bookmarkAdd(@RequestParam("userId") Integer userId,
                                         @RequestParam("boardId") Integer boardId) {
        log.info("\n북마크 추가 요청\n userId: {}, boardId: {}", userId, boardId);

        try {
            boardService.bookmarkAdd(userId, boardId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CustomResponse("북마크 추가에 성공하였습니다", Boolean.TRUE));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException() {
                @Override
                public HttpStatus getStatus() {
                    return HttpStatus.BAD_REQUEST;
                }

                @Override
                public String getMessage() {
                    return "북마크 추가에 실패하였습니다";
                }
            };
        }
    }

    @DeleteMapping("/bookmark/delete")
    public ResponseEntity<?> bookmarkDelete(@RequestParam("userId") Integer userId,
                                            @RequestParam("boardId") Integer boardId) {
        log.info("\n북마크 삭제 요청\n userId: {}, boardId: {}", userId, boardId);

        try {
            boardService.bookmarkDelete(userId, boardId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CustomResponse("북마크 삭제에 성공하였습니다", Boolean.FALSE));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException() {
                @Override
                public HttpStatus getStatus() {
                    return HttpStatus.BAD_REQUEST;
                }

                @Override
                public String getMessage() {
                    return "북마크 삭제에 실패하였습니다";
                }
            };
        }
    }

    @GetMapping("/like/add")
    public ResponseEntity<?> likeAdd(@RequestParam("userId") Integer userId,
                                     @RequestParam("boardId") Integer boardId) {
        log.info("\n좋아요 요청\n userId: {}, boardId: {}", userId, boardId);

        try {
            boardService.likeAdd(userId, boardId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CustomResponse("게시물 좋아요에 성공하였습니다", Boolean.TRUE));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException() {
                @Override
                public HttpStatus getStatus() {
                    return HttpStatus.BAD_REQUEST;
                }

                @Override
                public String getMessage() {
                    return "게시물 좋아요에 실패하였습니다";
                }
            };
        }
    }

    @DeleteMapping("/like/delete")
    public ResponseEntity<?> likeDelete(@RequestParam("userId") Integer userId,
                                        @RequestParam("boardId") Integer boardId) {
        log.info("\n좋아요 취소 요청\n userId: {}, boardId: {}", userId, boardId);

        try {
            boardService.likeDelete(userId, boardId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CustomResponse("좋아요 취소에 성공하였습니다", Boolean.FALSE));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException() {
                @Override
                public HttpStatus getStatus() {
                    return HttpStatus.BAD_REQUEST;
                }

                @Override
                public String getMessage() {
                    return "좋아요 취소에 실패하였습니다";
                }
            };
        }
    }

    @GetMapping("/comment")
    public ResponseEntity<?> getComments(@RequestParam("boardId") Integer boardId) {
        log.info("\n댓글리스트 요청 보드ID {}", boardId);

        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CustomResponse("게시글의 댓글 조회에 성공하였습니다", commentService.getComments(boardId)));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException() {
                @Override
                public HttpStatus getStatus() {
                    return HttpStatus.BAD_REQUEST;
                }

                @Override
                public String getMessage() {
                    return "게시글의 댓글 조회에 실패하였습니다";
                }
            };
        }
    }

    @PostMapping("/comment")
    public ResponseEntity<?> addComment(@RequestBody CommentRequestDto commentRequestDto) {
        log.info("\n댓글 추가 요청 Dto {}", commentRequestDto);

        try {
            // 댓글 객체로 받기
            Comment comment=commentService.addComment(commentRequestDto.getComment(),
                    commentRequestDto.getBoardId(), commentRequestDto.getUserId(), commentRequestDto.getCommentId());

            // 게시글 정보 조회
            Board board = boardDao.getBoardById(commentRequestDto.getBoardId());
            Integer postOwnerId = board.getUser().getId();
            Integer commentWriterId = comment.getUser().getId();

            // 부모 댓글 작성자 ID 조회 (대댓글일 경우)
            Integer parentCommentWriterId = null;
            if (comment.getParent() != null) {
                parentCommentWriterId = comment.getParent().getUser().getId();
            }

            // 알림 대상 유저 식별
            Set<Integer> targetUserIds = new HashSet<>();
            if (!postOwnerId.equals(commentWriterId)) {
                targetUserIds.add(postOwnerId);
            }
            if (parentCommentWriterId != null && !parentCommentWriterId.equals(commentWriterId)) {
                targetUserIds.add(parentCommentWriterId);
            }

            System.out.println("✅ 알림 대상 유저 ID 목록: " + targetUserIds);

            // 알림 전송 (예외는 무시)
            try {
                notificationService.sendBoardCommentNotification(comment);
            } catch (Exception e) {
                log.warn("게시판 댓글 알림 전송 실패: {}", e.getMessage());
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CustomResponse("댓글 저장에 성공하였습니다", null));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException() {
                @Override
                public HttpStatus getStatus() {
                    return HttpStatus.BAD_REQUEST;
                }

                @Override
                public String getMessage() {
                    return e.getMessage();
                }
            };
        }
    }

    @PutMapping("/comment")
    public ResponseEntity<?> updateComment(@RequestBody CommentRequestDto commentRequestDto,
                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("\n댓글 수정 요청 Dto {}", commentRequestDto);

        if(!userPrincipal.getUserId().equals(commentRequestDto.getUserId())) {
            throw new UnauthorizedException();
        }

        try {
            commentService.updateComment(commentRequestDto.getComment(), commentRequestDto.getCommentId());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CustomResponse("댓글 수정에 성공하였습니다", null));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException() {
                @Override
                public HttpStatus getStatus() {
                    return HttpStatus.BAD_REQUEST;
                }

                @Override
                public String getMessage() {
                    return e.getMessage();
                }
            };
        }
    }

    @DeleteMapping("/comment")
    public ResponseEntity<?> deleteComment(@RequestParam("userId") Integer userId,
                                           @RequestParam("commentId") Integer commentId,
                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("\n댓글 삭제 요청 \nuserId: {} commentId: {} ", userId, commentId);

        if(!userPrincipal.getUserId().equals(userId)) {
            throw new UnauthorizedException();
        }

        try {
            Comment comment = commentService.deleteComment(commentId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CustomResponse("댓글 삭제에 성공하였습니다", boardService.getBoardById(comment.getBoard().getId()).getComments()));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CustomException() {
                @Override
                public HttpStatus getStatus() {
                    return HttpStatus.BAD_REQUEST;
                }

                @Override
                public String getMessage() {
                    return "댓글 삭제에 실패하였습니다";
                }
            };
        }
    }

    @GetMapping("/type")
    public ResponseEntity<?> getBoardTypeList() {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CustomResponse(GET_BOARD_TYPE_SUCCESS.getMessage(), boardTypeService.getBoardTypeList()));
        } catch (Exception e) {
            throw new GetBoardTypeException();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchPost(@ModelAttribute SearchRequestDto searchRequestDto) {
        log.info("\n검색 요청 Dto: {} ", searchRequestDto);

        try {
            Pageable pageable = PageRequest.of(searchRequestDto.getPage(), searchRequestDto.getSize());

            Page<BoardResponseDto> posts;
            String keyword = searchRequestDto.getKeyword();
            if (keyword == null) {
                posts = boardService.getBoardsByType(searchRequestDto.getBoardTypeId(), pageable);
            } else {
                posts = boardService.getBoardsByTypeAndKeyword(searchRequestDto.getBoardTypeId(), keyword, pageable);
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CustomResponse(SEARCH_POST_SUCCESS.getMessage(), posts));
        } catch (Exception e) {
            throw new SearchPostException();
        }
    }
}
