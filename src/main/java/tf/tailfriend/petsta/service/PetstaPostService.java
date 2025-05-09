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
import tf.tailfriend.global.service.RedisService;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.global.service.StorageServiceException;
import tf.tailfriend.petsta.entity.*;
import tf.tailfriend.petsta.entity.dto.MentionDto;
import tf.tailfriend.petsta.entity.dto.PetstaCommentResponseDto;
import tf.tailfriend.petsta.entity.dto.PetstaPostResponseDto;
import tf.tailfriend.petsta.exception.PostNotFoundException;
import tf.tailfriend.petsta.repository.PetstaBookmarkDao;
import tf.tailfriend.petsta.repository.PetstaCommentDao;
import tf.tailfriend.petsta.repository.PetstaLikeDao;
import tf.tailfriend.petsta.repository.PetstaPostDao;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.repository.UserDao;
import tf.tailfriend.user.repository.UserFollowDao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    private final RedisService redisService;

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
        redisService.setStoryFlag(userId);

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
        redisService.setStoryFlag(userId);
    }



    @Transactional
    public List<PetstaPostResponseDto> getAllPosts(Integer loginUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 🔥 삭제되지 않은 게시글만 가져옴
        List<PetstaPost> posts = petstaPostDao.findAllByDeletedFalseOrderByCreatedAtDesc(pageable).getContent();

        return posts.stream()
                .map(post -> {
                    boolean initialLiked = petstaLikeDao.existsByUserIdAndPetstaPostId(loginUserId, post.getId());
                    boolean initialBookmarked = petstaBookmarkDao.existsByUserIdAndPetstaPostId(loginUserId, post.getId());
                    boolean initialFollowed = userFollowDao.existsByFollowerIdAndFollowedId(loginUserId, post.getUser().getId());
                    boolean isVisited = redisService.hasVisitedStory(post.getUser().getId(), loginUserId);

                    PetstaPostResponseDto dto = new PetstaPostResponseDto(post, initialLiked, initialBookmarked, initialFollowed, isVisited);

                    // 파일 presigned URL
                    String fileUrl = storageService.generatePresignedUrl(post.getFile().getPath());
                    dto.setFileName(fileUrl);

                    // 작성자 프로필 사진 presigned URL
                    User writer = post.getUser();
                    String userPhotoUrl = storageService.generatePresignedUrl(writer.getFile().getPath());
                    dto.setUserPhoto(userPhotoUrl);

                    return dto;
                })
                .collect(Collectors.toList());
    }


    @Transactional
    public PetstaPostResponseDto getPostById(Integer loginUserId, Integer postId) {
        PetstaPost post = petstaPostDao.findByIdAndDeletedFalse(postId)
                .orElseThrow(PostNotFoundException::new);

        boolean initialLiked = petstaLikeDao.existsByUserIdAndPetstaPostId(loginUserId, post.getId());
        boolean initialBookmarked = petstaBookmarkDao.existsByUserIdAndPetstaPostId(loginUserId, post.getId());
        boolean initialFollowed = userFollowDao.existsByFollowerIdAndFollowedId(loginUserId, post.getUser().getId());

        PetstaPostResponseDto dto = new PetstaPostResponseDto(post, initialLiked, initialBookmarked, initialFollowed, true);

        String fileUrl = storageService.generatePresignedUrl(post.getFile().getPath());
        dto.setFileName(fileUrl);

        String userPhotoUrl = storageService.generatePresignedUrl(post.getUser().getFile().getPath());
        dto.setUserPhoto(userPhotoUrl);

        return dto;
    }

    @Transactional
    public void updatePostContent(Integer userId, Integer postId, String newContent) throws AccessDeniedException {
        PetstaPost post = petstaPostDao.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new PostNotFoundException());

        if (!post.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("해당 게시글을 수정할 권한이 없습니다.");
        }

        post.setContent(newContent);
        // 엔티티 변경 감지 → 자동 flush
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
    public PetstaCommentResponseDto addComment(Integer postId, Integer userId, String content, Integer parentId, MentionDto mention) {
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

        // ⬇️ 멘션 처리
        PetstaCommentMention mentionEntity = null;
        if (mention != null) {
            User mentionedUser = userDao.findById(mention.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("멘션된 유저를 찾을 수 없습니다: " + mention.getUserId()));

            mentionEntity = PetstaCommentMention.builder()
                    .comment(comment)
                    .mentionedUser(mentionedUser)
                    .mentionedNickname(mention.getNickname())
                    .build();

            comment.setMention(mentionEntity);
        }

        PetstaComment savedComment = petstaCommentDao.save(comment);

        if (parent != null) {
            parent.addReply(savedComment);
            petstaCommentDao.save(parent);
        }

        petstaPostDao.incrementCommentCount(postId);

        String formattedCreatedAt = savedComment.getCreatedAt()
                .atZone(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);


        // ✅ 최종 응답 DTO 생성 및 반환
        return new PetstaCommentResponseDto(
                savedComment.getId(),
                savedComment.getContent(),
                user.getNickname(),
                user.getId(),
                storageService.generatePresignedUrl(user.getFile().getPath()),
                formattedCreatedAt,
                parentId,
                0,
                true,
                mention,
                false
        );
    }



    @Transactional
    public PetstaComment addCommententity(Integer postId, Integer userId, String content, Integer parentId) {
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

        if (parent != null) {
            parent.addReply(savedComment);
            petstaCommentDao.save(parent);
        }

        petstaPostDao.incrementCommentCount(postId);

        return savedComment;
    }


    @Transactional
    public List<PetstaCommentResponseDto> getParentCommentsByPostId(Integer currentId, Integer postId) {
        PetstaPost post = petstaPostDao.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + postId));

        return petstaCommentDao.findByPostAndParentIsNullOrderByCreatedAtDesc(post)
                .stream()
                .filter(comment -> !(comment.isDeleted() && comment.getReplyCount() == 0)) // 🔥 필터 조건
                .map(comment -> {
                    Integer commentUserId = comment.getUser().getId();
                    boolean isVisited = redisService.hasVisitedStory(commentUserId, currentId);

                    PetstaCommentMention mention = comment.getMention();
                    MentionDto mentionDto = null;
                    if (mention != null) {
                        mentionDto = new MentionDto(
                                mention.getMentionedUser().getId(),
                                mention.getMentionedNickname()
                        );
                    }

                    // ✅ createdAt을 KST 기준 문자열로 변환
                    String formattedCreatedAt = comment.getCreatedAt()
                            .atZone(ZoneId.of("Asia/Seoul"))
                            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                    return new PetstaCommentResponseDto(
                            comment.getId(),
                            comment.getContent(),
                            comment.getUser().getNickname(),
                            commentUserId,
                            storageService.generatePresignedUrl(comment.getUser().getFile().getPath()),
                            formattedCreatedAt, // ✅ 포맷된 시간
                            null,
                            comment.getReplyCount(),
                            isVisited,
                            mentionDto,
                            comment.isDeleted()
                    );
                })
                .collect(Collectors.toList());
    }



    @Transactional
    public List<PetstaCommentResponseDto> getReplyCommentsByCommentId(Integer commentId, Integer currentUserId) {
        PetstaComment parentComment = petstaCommentDao.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다: " + commentId));

        return petstaCommentDao.findByParentOrderByCreatedAtAsc(parentComment)
                .stream()
                .filter(reply -> !(reply.isDeleted() && reply.getReplyCount() == 0)) // 🔥 동일 필터
                .map(reply -> {
                    Integer replyUserId = reply.getUser().getId();
                    boolean isVisited = redisService.hasVisitedStory(replyUserId, currentUserId);

                    PetstaCommentMention mention = reply.getMention();
                    MentionDto mentionDto = null;
                    if (mention != null) {
                        mentionDto = new MentionDto(
                                mention.getMentionedUser().getId(),
                                mention.getMentionedNickname()
                        );
                    }

                    String formattedCreatedAt = reply.getCreatedAt()
                            .atZone(ZoneId.of("Asia/Seoul"))
                            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                    return new PetstaCommentResponseDto(
                            reply.getId(),
                            reply.getContent(),
                            reply.getUser().getNickname(),
                            replyUserId,
                            storageService.generatePresignedUrl(reply.getUser().getFile().getPath()),
                            formattedCreatedAt,
                            reply.getParent().getId(),
                            reply.getReplyCount(),
                            isVisited,
                            mentionDto,
                            reply.isDeleted() // 🔥 추가
                    );
                })
                .collect(Collectors.toList());
    }


    @Transactional
    public void deleteComment(Integer userId, Integer commentId) {
        PetstaComment comment = petstaCommentDao.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다: " + commentId));

        if (!comment.getUser().getId().equals(userId)) {
            throw new SecurityException("자신의 댓글만 삭제할 수 있습니다.");
        }

        comment.markAsDeleted(); // 삭제 표시

        // 부모 댓글이면 replyCount 감소
        if (comment.getParent() != null) {
            PetstaComment parent = comment.getParent();
            parent.setReplyCount(parent.getReplyCount() - 1);
            petstaCommentDao.save(parent);
        }

        // 포스트 전체 댓글 수 감소
        petstaPostDao.decrementCommentCount(comment.getPost().getId());

        // 삭제된 댓글 저장
        petstaCommentDao.save(comment);
    }
    @Transactional
    public void deletePost(Integer userId, Integer postId) {
        PetstaPost post = petstaPostDao.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다: " + postId));

        if (!post.getUser().getId().equals(userId)) {
            throw new SecurityException("자신의 게시물만 삭제할 수 있습니다.");
        }

        // 🔥 연관 좋아요 및 북마크 삭제
        petstaLikeDao.deleteAllByPostId(postId);

        petstaBookmarkDao.deleteAllByPostId(postId);

        post.markAsDeleted(); // 삭제 표시
        petstaPostDao.save(post);

        userDao.decrementPostCount(userId); // 작성글 수 감소
    }



}