package tf.tailfriend.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.admin.dto.AnnounceResponseDto;
import tf.tailfriend.admin.entity.Announce;
import tf.tailfriend.admin.entity.AnnouncePhoto;
import tf.tailfriend.admin.repository.AnnounceDao;
import tf.tailfriend.board.entity.BoardType;
import tf.tailfriend.file.entity.File;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnounceService {

    private final AnnounceDao announceDao;

    @Transactional
    public Announce createAnnounce(String title, String content, BoardType boardType, List<File> files) {

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

        if (files != null && !files.isEmpty()) {
            for (File file : files) {
                AnnouncePhoto photo = AnnouncePhoto.of(announce, file);
                announce.getPhotos().add(photo);
            }
        }

        return announceDao.save(announce);
    }
}
