package tf.tailfriend.global.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tf.tailfriend.reserve.dto.ReserveRequestDto;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

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



    // 예약 임시 저장
    public void saveTempReserve(String reserveKey, ReserveRequestDto dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            redisTemplate.opsForValue().set(reserveKey, json, Duration.ofMinutes(15));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("예약 정보를 Redis에 저장하는 데 실패했습니다.", e);
        }
    }

    // 예약 임시 조회
    public ReserveRequestDto getTempReserve(String reserveKey) {
        String json = redisTemplate.opsForValue().get(reserveKey);
        if (json == null) return null;

        try {
            return objectMapper.readValue(json, ReserveRequestDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis에서 예약 정보를 파싱할 수 없습니다.", e);
        }
    }

    // 예약 키 삭제
    public void deleteTempReserve(String reserveKey) {
        redisTemplate.delete(reserveKey);
    }


}
