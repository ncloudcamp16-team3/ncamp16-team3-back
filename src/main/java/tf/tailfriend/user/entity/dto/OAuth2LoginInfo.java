package tf.tailfriend.user.entity.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OAuth2LoginInfo {
    private String email;
    private String snsAccountId;
    private Integer snsTypeId;
    private String token;
}