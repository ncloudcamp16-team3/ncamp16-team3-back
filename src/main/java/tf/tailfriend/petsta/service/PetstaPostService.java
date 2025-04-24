package tf.tailfriend.petsta.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.service.FileService;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.global.service.StorageServiceException;
import tf.tailfriend.petsta.entity.PetstaBookmark;
import tf.tailfriend.petsta.entity.PetstaComment;
import tf.tailfriend.petsta.entity.PetstaLike;
import tf.tailfriend.petsta.entity.PetstaPost;
import tf.tailfriend.petsta.entity.dto.PetstaCommentResponseDto;
import tf.tailfriend.petsta.entity.dto.PetstaPostResponseDto;
import tf.tailfriend.petsta.repository.PetstaBookmarkDao;
import tf.tailfriend.petsta.repository.PetstaCommentDao;
import tf.tailfriend.petsta.repository.PetstaLikeDao;
import tf.tailfriend.petsta.repository.PetstaPostDao;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.repository.UserDao;
import tf.tailfriend.user.repository.UserFollowDao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetstaPostService {

    private final FileService fileService;
    private final StorageService storageService;
    private final PetstaPostDao petstaPostDao;
    private final PetstaLikeDao petstaLikeDao;
    private final PetstaBookmarkDao petstaBookmarkDao;
    private final UserDao userDao;
    private final UserFollowDao userFollowDao;
    private final PetstaCommentDao petstaCommentDao;

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
        userDao.incrementPostCount(userId);

    }


    @Transactional
    public void uploadVideo(Integer userId, String content, String trimStart, String trimEnd, MultipartFile videoFile)
            throws StorageServiceException, IOException, InterruptedException {

        // 1. 동영상 잘라내기
        Path trimmedVideo = fileService.trimVideo(videoFile, trimStart, trimEnd);

        // 2. 파일 엔티티 저장 (파일명은 Path에서 가져와야 함)
        File savedFile = fileService.save(trimmedVideo.getFileName().toString(), "post", File.FileType.VIDEO);

        // 3. 썸네일 추출 (중간 시간)
        double duration = fileService.getVideoDurationInSeconds(trimmedVideo); // ffprobe 필요
        Path thumbnailPath = fileService.extractThumbnail(trimmedVideo, duration / 2);

        File thumbnailFile = fileService.save(thumbnailPath.getFileName().toString(), "post", File.FileType.PHOTO);

        // 4. 업로드
        try (InputStream videoIs = Files.newInputStream(trimmedVideo);
             InputStream thumbIs = Files.newInputStream(thumbnailPath)) {

            storageService.upload(savedFile.getPath(), videoIs);
            storageService.upload(thumbnailFile.getPath(), thumbIs);

        } catch (IOException | StorageServiceException e) {
            throw new StorageServiceException(e);
        } finally {
            // 5. 임시 파일 삭제
            Files.deleteIfExists(trimmedVideo);
            Files.deleteIfExists(thumbnailPath);
        }

        // 6. 유저 조회
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 7. 게시글 저장
        PetstaPost post = PetstaPost.builder()
                .user(user)
                .file(savedFile)
                .thumbnailFile(thumbnailFile) // ✅ 썸네일 연결
                .content(content)
                .build();

        petstaPostDao.save(post);
        userDao.incrementPostCount(userId);
    }



    @Transactional
    public List<PetstaPostResponseDto> getAllPosts(Integer loginUserId) {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<PetstaPost> posts = petstaPostDao.findAllByOrderByCreatedAtDesc(pageable).getContent();


        return posts.stream()
                .map(post -> {
                    boolean initialLiked = petstaLikeDao.existsByUserIdAndPetstaPostId(loginUserId, post.getId());
                    boolean initialBookmarked = petstaBookmarkDao.existsByUserIdAndPetstaPostId(loginUserId, post.getId());
                    boolean initialFollowed = userFollowDao.existsByFollowerIdAndFollowedId(loginUserId, post.getUser().getId());


                    PetstaPostResponseDto dto = new PetstaPostResponseDto(post, initialLiked, initialBookmarked, initialFollowed);

                    // 게시글 파일 URL
                    String fileUrl = storageService.generatePresignedUrl(post.getFile().getPath());
                    dto.setFileName(fileUrl);

                    // 글쓴이(유저) 정보
                    User writer = post.getUser();

                    String userPhotoUrl = storageService.generatePresignedUrl(writer.getFile().getPath());
                    dto.setUserPhoto(userPhotoUrl);
                    System.out.println(userPhotoUrl);

                    return dto;
                })

                .collect(Collectors.toList());
    }

    @Transactional
    public PetstaPostResponseDto getPostById(Integer loginUserId, Integer postId) {
        // 1. postId로 게시글 조회
        PetstaPost post = petstaPostDao.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시글을 찾을 수 없습니다."));

        // 2. 좋아요, 북마크 여부 조회
        boolean initialLiked = petstaLikeDao.existsByUserIdAndPetstaPostId(loginUserId, post.getId());
        boolean initialBookmarked = petstaBookmarkDao.existsByUserIdAndPetstaPostId(loginUserId, post.getId());
        boolean initialFollowed = userFollowDao.existsByFollowerIdAndFollowedId(loginUserId, post.getUser().getId());

        // 3. DTO 생성
        PetstaPostResponseDto dto = new PetstaPostResponseDto(post, initialLiked, initialBookmarked, initialFollowed);

        // 4. 게시글 파일 presigned URL 생성
        String fileUrl = storageService.generatePresignedUrl(post.getFile().getPath());
        dto.setFileName(fileUrl);

        // 5. 글쓴이 프로필사진 presigned URL 생성
        User writer = post.getUser();

        String userPhotoUrl = storageService.generatePresignedUrl(writer.getFile().getPath());
        dto.setUserPhoto(userPhotoUrl);

        return dto;
    }




    @Transactional
    public void toggleLike(Integer userId, Integer postId) {
        PetstaPost post = petstaPostDao.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Optional<PetstaLike> existingLike = petstaLikeDao.findByUserIdAndPetstaPostId(userId, postId);

        if (existingLike.isPresent()) {
            petstaLikeDao.delete(existingLike.get());
            petstaPostDao.decrementLikeCount(postId);
        } else {
            PetstaLike newLike = PetstaLike.of(user, post); // << 깔끔
            petstaLikeDao.save(newLike);
            petstaPostDao.incrementLikeCount(postId);
        }
    }


    @Transactional
    public void toggleBookmark(Integer userId, Integer postId) {
        PetstaPost post = petstaPostDao.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Optional<PetstaBookmark> existingBookmark = petstaBookmarkDao.findByUserIdAndPetstaPostId(userId, postId);

        if (existingBookmark.isPresent()) {
            petstaBookmarkDao.delete(existingBookmark.get());
        } else {
            PetstaBookmark newBookmark = PetstaBookmark.of(user, post); // << 깔끔
            petstaBookmarkDao.save(newBookmark);
        }
    }

    @Transactional
    public void addComment(Integer postId, Integer userId, String content, Integer parentId) {
        PetstaPost post = petstaPostDao.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + postId));

        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다: " + userId));


        PetstaComment parent = null;
        if (parentId != null) {
            parent = petstaCommentDao.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다: " + parentId));
        }

        PetstaComment comment = PetstaComment.builder()
                .post(post)
                .user(user)
                .content(content)
                .parent(parent)
                .build();

        PetstaComment savedComment = petstaCommentDao.save(comment);

        // 부모 댓글이 있으면, 대댓글 추가 처리
        if (parent != null) {
            parent.addReply(savedComment);
            petstaCommentDao.save(parent); // replyCount 증가 저장
        }

        petstaPostDao.incrementCommentCount(postId);

    }

    @Transactional
    public List<PetstaCommentResponseDto> getParentCommentsByPostId(Integer postId) {
        PetstaPost post = petstaPostDao.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + postId));

        return petstaCommentDao.findByPostAndParentIsNullOrderByCreatedAtDesc(post)
                .stream()
                .map(comment -> new PetstaCommentResponseDto(
                        comment.getId(),
                        comment.getContent(),
                        comment.getUser().getNickname(),
                        comment.getUser().getId(),
                        storageService.generatePresignedUrl(comment.getUser().getFile().getPath()),
                        comment.getCreatedAt(),
                        null,
                        comment.getReplyCount(),
                        true
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PetstaCommentResponseDto> getReplyCommentsByCommentId(Integer commentId) {
        PetstaComment parentComment = petstaCommentDao.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다: " + commentId));

        return petstaCommentDao.findByParentOrderByCreatedAtAsc(parentComment)
                .stream()
                .map(reply -> new PetstaCommentResponseDto(
                        reply.getId(),
                        reply.getContent(),
                        reply.getUser().getNickname(),
                        reply.getUser().getId(),
                        storageService.generatePresignedUrl(reply.getUser().getFile().getPath()),
                        reply.getCreatedAt(),
                        reply.getParent().getId(),
                        reply.getReplyCount(),
                        false // 자식 댓글은 isView=false (처음엔 안 펼쳐져 있음)
                ))
                .collect(Collectors.toList());
    }




}