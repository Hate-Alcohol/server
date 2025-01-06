package org.example.hatealcohol.common.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.hatealcohol.gps.exception.RedisGetObjectListException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisUtil {

  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;

  public <T> void saveObjectList(String key, T object) {
    redisTemplate.opsForList().rightPush(key, object); // 순차 저장
  }

  public <T> void saveObjectList(String key, T object, long expiration) {
    redisTemplate.opsForList().rightPush(key, object);
    redisTemplate.expire(key, Duration.ofSeconds(expiration));
  }

  public <T> List<T> getObjectList(String key, Class<T> clazz) {

    try {
      List<Object> redisData = redisTemplate.opsForList().range(key, 0, -1); // 처음부터 끝 인덱스

      List<T> list = new ArrayList<>();
      for (Object obj : redisData) {
        list.add(objectMapper.convertValue(obj, clazz));
      }

      return list;
    } catch (Exception e) {
      throw new RedisGetObjectListException("Redis에서 리스트를 가져오지 못했습니다.: " + e.getMessage());
    }
  }

  public void deleteLocationHistory(String key) {
    redisTemplate.delete(key);
  }

  public String createKey(String prefix, String id) {
    return prefix + ":" + id;
  }
}
