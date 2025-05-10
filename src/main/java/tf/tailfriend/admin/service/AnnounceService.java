package tf.tailfriend.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.admin.dto.AnnounceResponseDto;
import tf.tailfriend.admin.entity.Announce;
import tf.tailfriend.admin.entity.AnnouncePhoto;
import tf.tailfriend.admin.repository.AnnounceDao;
import tf.tailfriend.board.dto.AnnounceDto;
import tf.tailfriend.board.entity.BoardType;
import tf.tailfriend.board.exception.GetAnnounceDetailException;
import tf.tailfriend.board.repository.BoardTypeDao;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.service.FileService;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.global.service.StorageServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnounceService {

    private final AnnounceDao announceDao;
    private final FileService fileService;
    private final StorageService storageService;
    private final BoardTypeDao boardTypeDao;

    @Transactional
    public Announce createAnnounce(String title, String content, BoardType boardType, List<MultipartFile> images) {

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("공지사항 제목은 필수입니다.");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("공지사항 내용은 필수입니다.");
        }

        if (boardType == null) {
            throw new IllegalArgumentException("게시판 타입은 필수입니다.");
        }

        Announce announce = Announce.builder()
                .title(title)
                .content(content)
                .boardType(boardType)
                .build();

        List<File> files = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    // 1. 파일 메타데이터를 DB에 저장
                    File savedFile = fileService.save(image.getOriginalFilename(), "announce", File.FileType.PHOTO);
                    files.add(savedFile);

                    // 2. 실제 파일을 S3에 업로드
                    try (InputStream is = image.getInputStream()) {
                        storageService.upload(savedFile.getPath(), is);
                    } catch (IOException e) {
                        try {
                            throw new StorageServiceException("파일 업로드 실패: " + e.getMessage(), e);
                        } catch (StorageServiceException ex) {
                            throw new RuntimeException(ex);
                        }
                    } catch (StorageServiceException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        // 3. 공지사항에 파일들을 연결
        if (!files.isEmpty()) {
            for (File file : files) {
                AnnouncePhoto photo = AnnouncePhoto.of(announce, file);
                announce.getPhotos().add(photo);
            }
        }


        return announceDao.save(announce);

    }

    @Transactional(readOnly = true)
    public Page<AnnounceResponseDto> searchAnnounces(String searchTerm, String searchField,
                                                     Integer boardTypeId, Pageable pageable) {
        BoardType boardType = boardTypeDao.findById(boardTypeId)
                .orElseThrow(() -> new IllegalArgumentException("게시판 타입이 유효하지 않습니다 " + boardTypeId));
        Page<Announce> announces;

        switch (searchField) {
            case "title":
                if (boardType != null) {
                    announces = announceDao.findByTitleContainingAndBoardType(searchTerm, boardType, pageable);
                } else {
                    announces = announceDao.findByTitleContaining(searchTerm, pageable);
                }
                break;
            case "content":
                if (boardType != null) {
                    announces = announceDao.findByContentContainingAndBoardType(searchTerm, boardType, pageable);
                } else {
                    announces = announceDao.findByContentContaining(searchTerm, pageable);
                }
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 검색 필드입니다: " + searchField);
        }

        return announces.map(announce -> AnnounceResponseDto.fromEntity(announce, storageService));
    }

    @Transactional(readOnly = true)
    public Page<AnnounceResponseDto> getAnnouncesByType(Integer boardTypeId, Pageable pageable) {
        BoardType boardType = boardTypeDao.findById(boardTypeId)
                .orElseThrow(() -> new IllegalArgumentException("게시판 타입이 유효하지 않습니다: " + boardTypeId));
        Page<Announce> announces = announceDao.findByBoardType(boardType, pageable);
        return announces.map(announce -> AnnounceResponseDto.fromEntity(announce, storageService));
    }

    @Transactional(readOnly = true)
    public Page<AnnounceResponseDto> getAllAnnounces(Pageable pageable) {
        Page<Announce> announces = announceDao.findAll(pageable);
        return announces.map(announce -> AnnounceResponseDto.fromEntity(announce, storageService));
    }

    @Transactional(readOnly = true)
    public List<AnnounceDto> getAnnounces(Integer boardTypeId) {

        List<Announce> announces = announceDao.findByBoardTypeIdOrderByCreatedAtDesc(boardTypeId);
        List<AnnounceDto> announceDtos = new ArrayList<>();

        for(Announce item: announces) {
            announceDtos.add(AnnounceDto.fromEntity(item));
        }

        return announceDtos;
    }

    @Transactional(readOnly = true)
    public AnnounceDto getAnnounceDetail(Integer announceId) {

        Announce announce = announceDao.findById(announceId)
                .orElseThrow(() -> new GetAnnounceDetailException());
        AnnounceDto responseAnnounceDto = AnnounceDto.fromEntity(announce);

        if(!responseAnnounceDto.getPhotos().isEmpty()) {
            responseAnnounceDto.setPhotos(makePresignedPath(responseAnnounceDto.getPhotos()));
        }

        return responseAnnounceDto;
    }

    @Transactional
    public void deleteAnnounceById(Integer id) {
        announceDao.deleteById(id);
    }

    private List<String> makePresignedPath(List<String> paths) {

        return paths.stream()
                .map(path -> storageService.generatePresignedUrl(path))
                .collect(Collectors.toList());
    }
}
