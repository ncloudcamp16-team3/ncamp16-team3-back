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
    public File save(String originName, String pathName, File.FileType fileType) {
        String extension = "";
        if (originName != null && originName.contains(".")) {
            extension = originName.substring(originName.lastIndexOf(".") + 1);
        }
        String uuid = UUID.randomUUID().toString();
        String path = "uploads/" + pathName + "/" + uuid + (extension.isEmpty() ? "" : "." + extension);

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
