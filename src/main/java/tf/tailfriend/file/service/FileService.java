package tf.tailfriend.file.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.repository.FileDao;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileDao fileDao;

    @Transactional
    public File save(String pathName, File.FileType fileType) {
        String uuid = UUID.randomUUID().toString();
        String path = "uploads/" + pathName + "/" + uuid;

        File file = File.builder()
                .path(path)
                .type(fileType)
                .build();

        return fileDao.save(file);
    }

    @Transactional(readOnly = true)
    public File getOrDefault(Integer fileId) {
        Integer targetId = (fileId != null) ? fileId : 1;
        return fileDao.findById(targetId)
                .orElseThrow(() -> new RuntimeException("기본 파일 없음"));
    }
}
