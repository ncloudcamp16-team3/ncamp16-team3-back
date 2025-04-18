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
import tf.tailfriend.petsta.repository.PetstaPostDao;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.repository.UserDao;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PetstaPostService {

    private final FileService fileService;
     private final StorageService storageService;
     private final PetstaPostDao petstaPostDao;
     private final UserDao userDao;

    @Transactional
    public void uploadPost(Integer userId, String content, MultipartFile imageFile) throws StorageServiceException {
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


}