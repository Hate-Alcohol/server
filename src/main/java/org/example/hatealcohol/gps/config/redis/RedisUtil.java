package org.example.hatealcohol.gps.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.hatealcohol.gps.dto.LocationHistoryRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisUtil {

  private final RedisTemplate<String, Object> redisTemplate;

  public void saveLocationHistory(Long sessionId, LocationHistoryRequest locationRequest) {

    String key = createLocationHistoryKey(sessionId);
    redisTemplate.opsForList().rightPush(key, locationRequest); // 순차 저장
  }

  public List<LocationHistoryRequest> getLocationHistory(Long sessionId) {

    String key = createLocationHistoryKey(sessionId);
    List<Object> redisData = redisTemplate.opsForList().range(key, 0, -1); // 처음부터 끝 인덱스

    ObjectMapper mapper = new ObjectMapper();
    List<LocationHistoryRequest> historyRequest = new ArrayList<>();

    for (Object obj : redisData) {
      historyRequest.add(
          mapper.convertValue(obj, LocationHistoryRequest.class)
      );
    }

    return historyRequest;
  }

  public void deleteLocationHistory(Long sessionId) {

    String key = createLocationHistoryKey(sessionId);
    redisTemplate.delete(key);
  }

  private String createLocationHistoryKey(Long sessionId) {
    return "location_history:" + sessionId;
  }
}
