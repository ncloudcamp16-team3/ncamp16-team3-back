package tf.tailfriend.petmeeting.dto;

public interface PetFriendProjection {
    Integer getId();
    String getName();
    String getGender();
    String getBirth();
    Double getWeight();
    String getInfo();
    Boolean getNeutered();
    String getActivityStatus();
    Double getDistance();
}
