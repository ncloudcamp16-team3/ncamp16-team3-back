package tf.tailfriend.admin.service;

import lombok.RequiredArgsConstructor;
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
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.service.FileService;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.global.service.StorageServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnounceService {

    private final AnnounceDao announceDao;
    private final FileService fileService;
    private final StorageService storageService;

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
        if (images != null & !images.isEmpty()) {
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
    public List<AnnounceDto> getAnnounces(Integer boardTypeId) {

        List<Announce> announces = announceDao.findByBoardType_Id(boardTypeId);
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
        AnnounceDto.fromEntity(announce);

        return AnnounceDto.fromEntity(announce);
    }
}
