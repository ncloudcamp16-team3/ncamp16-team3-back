package tf.tailfriend.user.config;;

import org.springframework.stereotype.Component;
import tf.tailfriend.user.entity.dto.OAuth2LoginInfo;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OAuth2SessionStorage {

    private static final long EXPIRATION_SECONDS = 300; // 5분
    private final Map<String, SessionWrapper> storage = new ConcurrentHashMap<>();

    public String save(OAuth2LoginInfo info) {
        String sessionId = UUID.randomUUID().toString();
        storage.put(sessionId, new SessionWrapper(info));
        return sessionId;
    }

    public OAuth2LoginInfo get(String sessionId) {
        SessionWrapper wrapper = storage.get(sessionId);
        if (wrapper == null) return null;

        // 만료되었는지 확인
        if (Instant.now().isAfter(wrapper.expireAt)) {
            storage.remove(sessionId);
            return null;
        }

        return wrapper.info;
    }

    public void remove(String sessionId) {
        storage.remove(sessionId);
    }

    private static class SessionWrapper {
        OAuth2LoginInfo info;
        Instant expireAt;

        SessionWrapper(OAuth2LoginInfo info) {
            this.info = info;
            this.expireAt = Instant.now().plusSeconds(EXPIRATION_SECONDS);
        }
    }
}
