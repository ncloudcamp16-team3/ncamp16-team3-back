package tf.tailfriend.petsta.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.service.FileService;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.global.service.StorageServiceException;
import tf.tailfriend.petsta.entity.PetstaPost;
import tf.tailfriend.petsta.entity.dto.PostResponseDto;
import tf.tailfriend.petsta.repository.PetstaPostDao;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.repository.UserDao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetstaPostService {

     private final FileService fileService;
     private final StorageService storageService;
     private final PetstaPostDao petstaPostDao;
     private final UserDao userDao;

    @Transactional
    public void uploadPhoto(Integer userId, String content, MultipartFile imageFile) throws StorageServiceException {
        // 1. 파일 저장
        File savedFile = fileService.save(imageFile.getOriginalFilename(), "post", File.FileType.PHOTO);

        // 2. 파일 S3 업로드
        try (InputStream is = imageFile.getInputStream()) {
            storageService.upload(savedFile.getPath(), is);
        } catch (IOException | StorageServiceException e) {
            throw new StorageServiceException(e);
        }

        // 3. 유저 객체 조회
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 4. 게시물 저장
        PetstaPost post = PetstaPost.builder()
                .user(user)
                .file(savedFile)
                .content(content)
                .build();

        petstaPostDao.save(post);
    }


    @Transactional
    public void uploadVideo(Integer userId, String content, String trimStart, String trimEnd, MultipartFile videoFile) throws StorageServiceException, IOException, InterruptedException {
        // 1. 동영상 잘라내기
        Path trimmedVideo = fileService.trimVideo(videoFile, trimStart, trimEnd);

        // 2. 파일 엔티티 저장 (파일명은 Path에서 가져와야 함)
        File savedFile = fileService.save(trimmedVideo.getFileName().toString(), "post", File.FileType.VIDEO);

        // 3. S3 업로드
        try (InputStream is = Files.newInputStream(trimmedVideo)) {
            storageService.upload(savedFile.getPath(), is);
        } catch (IOException | StorageServiceException e) {
            throw new StorageServiceException(e);
        } finally {
            // 3-1. 업로드 끝났으면 임시 파일 삭제 (깨끗하게)
            Files.deleteIfExists(trimmedVideo);
        }

        // 4. 유저 조회
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 5. 게시글 저장
        PetstaPost post = PetstaPost.builder()
                .user(user)
                .file(savedFile)
                .content(content)
                .build();

        petstaPostDao.save(post);
    }


    @Transactional
    public List<PostResponseDto> getAllPosts() {
        // PetstaPostDao를 통해 모든 게시글을 조회
        List<PetstaPost> posts = petstaPostDao.findAll();

        // PetstaPost를 PostResponseDto로 변환
        return posts.stream()  .map(post -> {
                    PostResponseDto dto = new PostResponseDto(post);
                    // 파일 경로를 기반으로 S3 링크 생성
                    String fileUrl = storageService.generatePresignedUrl(post.getFile().getPath());
                    dto.setFileName(fileUrl); // PostResponseDto에 파일 URL 추가
                    return dto;
                })
                .collect(Collectors.toList());
    }



}