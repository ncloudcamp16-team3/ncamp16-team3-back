package tf.tailfriend.file.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.repository.FileDao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
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

        System.out.println("저장된 파일 경로:" + path);

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

    public Path trimVideo(MultipartFile videoFile, String trimStart, String trimEnd) throws IOException, InterruptedException {
        // 1. 임시 파일 생성
        Path originalPath = Files.createTempFile("original_", "_" + videoFile.getOriginalFilename());
        Path trimmedPath = Files.createTempFile("trimmed_", "_" + videoFile.getOriginalFilename());

        // 2. MultipartFile -> Temp 파일로 저장
        videoFile.transferTo(originalPath.toFile());

        String[] command = {
                "ffmpeg",
                "-y",
                "-ss", trimStart,
                "-i", originalPath.toAbsolutePath().toString(),
                "-to", trimEnd,
                "-c:v", "libx264",
                "-crf", "23",
                trimmedPath.toAbsolutePath().toString()
        };


        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true); // 표준 에러도 같이 읽음
        Process process = builder.start();

        // ★ 여기 추가 - ffmpeg 출력 읽기
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // ffmpeg 로그 출력
            }
        }

        int result = process.waitFor();
        if (result != 0) {
            throw new RuntimeException("동영상 자르기 실패");
        }
        // 4. 원본 파일 삭제
        Files.deleteIfExists(originalPath);

        // 5. 잘린 Path만 리턴
        return trimmedPath;
    }
    @Transactional(readOnly = true)
    public File getDefaultImage() {
        return getOrDefault(1); // 1번은 기본 이미지라고 가정
    }

    public double getVideoDurationInSeconds(Path videoPath) throws IOException, InterruptedException {
        String[] command = {
                "ffprobe",
                "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                videoPath.toAbsolutePath().toString()
        };

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        Process process = builder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String output = reader.readLine();
            return Double.parseDouble(output);
        }
    }

    public Path extractThumbnail(Path videoPath, double middleSecond) throws IOException, InterruptedException {
        Path thumbnailPath = Files.createTempFile("thumbnail_", ".jpg");

        String middleTime = String.format("00:%02d:%02d", (int)(middleSecond / 60), (int)(middleSecond % 60));

        String[] command = {
                "ffmpeg",
                "-y",
                "-ss", middleTime,
                "-i", videoPath.toAbsolutePath().toString(),
                "-vframes", "1",
                thumbnailPath.toAbsolutePath().toString()
        };

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        Process process = builder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int result = process.waitFor();
        if (result != 0) {
            throw new RuntimeException("썸네일 추출 실패");
        }

        return thumbnailPath;
    }

    @Transactional(readOnly = true)
    public String getFullUrl(String path) {
        final String BASE_URL = "https://kr.object.ncloudstorage.com/tailfriends-buck/";
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path; // 이미 완전한 URL이면 그대로 반환
        }
        return BASE_URL + path;
    }




}
