package tf.tailfriend.global.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public void setStoryFlag(Integer userId) {
        String key = "story:" + userId;
        redisTemplate.opsForValue().set(key, "true", Duration.ofHours(24));
        redisTemplate.delete("story:visited:" + userId); // 새 스토리 → 기존 방문자 기록 초기화
    }

    public void markStoryVisited(Integer storyOwnerId, Integer visitorId) {
        String storyKey = "story:" + storyOwnerId;
        String visitedKey = "story:visited:" + storyOwnerId;

        // 스토리가 있는 경우에만 방문 기록 저장
        if (Boolean.TRUE.equals(redisTemplate.hasKey(storyKey))) {
            redisTemplate.opsForSet().add(visitedKey, visitorId.toString());
            redisTemplate.expire(visitedKey, Duration.ofHours(24)); // TTL 유지 보장
        }
    }

    public boolean hasVisitedStory(Integer storyOwnerId, Integer currentUserId) {
        // 자기 자신이면 무조건 true
        if (storyOwnerId.equals(currentUserId)) {
            return true;
        }

        // 스토리가 없으면 방문 여부 따질 필요 없음 → true
        String storyKey = "story:" + storyOwnerId;
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(storyKey))) {
            return true;
        }

        // 스토리가 있으면 방문 여부 확인
        String visitedKey = "story:visited:" + storyOwnerId;
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(visitedKey, currentUserId.toString())
        );
    }


    public boolean hasStory(Integer userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("story:" + userId));
    }

    public void addVisitor(Integer targetUserId, Integer visitorId) {
        String key = "story:visited:" + targetUserId;
        redisTemplate.opsForSet().add(key, visitorId.toString());
        redisTemplate.expire(key, Duration.ofHours(24)); // TTL 연장
    }

    public boolean hasVisited(Integer targetUserId, Integer visitorId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(
                "story:visited:" + targetUserId, visitorId.toString()));
    }
}
