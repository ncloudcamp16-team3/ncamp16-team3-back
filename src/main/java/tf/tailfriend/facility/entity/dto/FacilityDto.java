//package tf.tailfriend.facility.entity.dto;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import lombok.ToString;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Getter
//@Setter
//@ToString
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class FacilityDto {
//    private Integer id;
//    private FacilityTypeDto facilityType;
//    private String name;
//    private String tel;
//    private String comment;
//    private Double starPoint;
//    private Integer reviewCount;
//    private String address;
//    private String detailAddress;
//    private Double latitude;
//    private Double longitude;
//
//    @Builder.Default
//    private List<FacilityPhotoDto> photos = new ArrayList<>();
//
//    @Builder.Default
//    private List<FacilityTimetableDto> timetables = new ArrayList<>();
//}