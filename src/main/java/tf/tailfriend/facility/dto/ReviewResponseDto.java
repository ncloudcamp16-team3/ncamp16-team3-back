package tf.tailfriend.facility.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {

    private Integer id;
    private String comment;
    private Integer starPoint;
    private LocalDateTime createdAt;
    private Integer userId;
    private String userName;
    private String userProfileImage;

    // 필요에 따라 이미지 URL 리스트 추가
    private List<String> reviewImages;

    // 날짜를 포맷팅된 문자열로 반환하는 메서드
    public String getFormattedDate() {
        if (createdAt == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return createdAt.format(formatter);
    }
}
