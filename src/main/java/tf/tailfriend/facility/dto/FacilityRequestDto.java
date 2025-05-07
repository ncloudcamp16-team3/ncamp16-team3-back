package tf.tailfriend.facility.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FacilityRequestDto {

    private String name;
    private Integer facilityTypeId;
    private String tel;
    private String address;
    private String detailAddress;
    private String comment;
    private Double latitude;
    private Double longitude;

    // 요일별 운영시간을 위한 필드
    private Map<String, String> openTimes;
    private Map<String, String> closeTimes;
    private Map<String, Boolean> openDays; // 요일별 영업 여부
}
