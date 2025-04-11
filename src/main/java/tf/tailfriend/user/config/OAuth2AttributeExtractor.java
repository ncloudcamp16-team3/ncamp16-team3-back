package tf.tailfriend.user.config;

import java.util.Map;

public class OAuth2AttributeExtractor {

    public static String getEmail(Map<String, Object> attributes) {
        if (attributes.containsKey("email")) {
            return (String) attributes.get("email"); // Google
        } else if (attributes.containsKey("kakao_account")) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            return (String) kakaoAccount.get("email");
        } else if (attributes.containsKey("response")) {
            Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");
            return (String) naverResponse.get("email");
        }
        return "unknown";
    }

    public static String getSnsAccountId(Map<String, Object> attributes) {
        return getEmail(attributes);
//        if (attributes.containsKey("sub")) {
//            return (String) attributes.get("sub"); // Google
//        } else if (attributes.containsKey("id")) {
//            return String.valueOf(attributes.get("id")); // Kakao
//        } else if (attributes.containsKey("response")) {
//            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
//            return (String) response.get("id"); // Naver
//        }
//        return "unknown";
    }

    public static Integer getSnsTypeId(Map<String, Object> attributes) {
        if (attributes.containsKey("sub")) {
            return 3; // Google
        } else if (attributes.containsKey("id")) {
            return 1; // Kakao
        } else if (attributes.containsKey("response")) {
            return 2; // Naver
        }
        return null;
    }
}