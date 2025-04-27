package tf.tailfriend.file.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.service.FileService;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.global.service.StorageServiceException;
import tf.tailfriend.user.service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;
    private final StorageService storageService;

    /**
     * 파일 업로드 처리
     * @param file 업로드된 파일
     * @return 파일 ID와 경로
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 파일 타입 결정
            File.FileType fileType = file.getContentType().startsWith("image/")
                    ? File.FileType.PHOTO
                    : File.FileType.VIDEO;

            // DB에 파일 메타데이터 저장
            File savedFile = fileService.save(file.getOriginalFilename(), "profiles", fileType);

            // 파일 스토리지에 업로드
            try (InputStream inputStream = file.getInputStream()) {
                storageService.upload(savedFile.getPath(), inputStream);
            } catch (StorageServiceException e) {
                log.error("Storage 업로드 실패", e);
                return ResponseEntity.internalServerError().body(Map.of("error", "파일 저장에 실패했습니다."));
            }

            // 응답 생성
            Map<String, Object> response = new HashMap<>();
            response.put("fileId", savedFile.getId());
            response.put("filePath", savedFile.getPath());
            response.put("fileUrl", storageService.generatePresignedUrl(savedFile.getPath()));

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("파일 업로드 실패", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "파일 처리 중 오류가 발생했습니다."));
        }
    }
}