package tf.tailfriend.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.admin.dto.AnnounceResponseDto;
import tf.tailfriend.admin.entity.Announce;
import tf.tailfriend.admin.service.AnnounceService;
import tf.tailfriend.board.entity.BoardType;
import tf.tailfriend.board.service.BoardTypeService;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.service.FileService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminAnnounceController {

    private final BoardTypeService boardTypeService;
    private final AnnounceService announceService;
    private final FileService fileService;

    @PostMapping("/announce/post")
    public ResponseEntity<?> createAnnounce(
            @RequestParam("boardTypeId") Integer boardTypeId,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        try {
            log.info("/announce/post, boardTypeId: {}", boardTypeId);
            BoardType boardType = boardTypeService.getBoardTypeById(boardTypeId);
            if (boardType == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "유효하지 않은 게시판 타입입니다"));
            }

            List<File> files = new ArrayList<>();
            if (images != null && !images.isEmpty()) {
                for (MultipartFile image : images) {
                    if (!image.isEmpty()) {
                        File file = fileService.save("board", File.FileType.PHOTO);
                        files.add(file);
                    }
                }
            }

            Announce announce = announceService.createAnnounce(title, content, boardType, files);
            AnnounceResponseDto responseDto = AnnounceResponseDto.fromEntity(announce);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "공지사항이 성공정으로 등록되었습니다"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "공지사항 등록 실패: " + e.getMessage()));
        }
    }
}
