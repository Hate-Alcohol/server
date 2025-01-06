package org.example.hatealcohol.gps.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.hatealcohol.common.redis.RedisUtil;
import org.example.hatealcohol.gps.dto.LocationHistoryRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationService {

  private final RedisUtil redisUtil;
  private final long LOCATION_EXPIRATION = 172800; // 2일
  private final String LOCATION_PREFIX = "location_history";

  public void saveLocationList(String sessionId, LocationHistoryRequest locationHistoryRequest) {

    validateLocationData(locationHistoryRequest);
    String key = redisUtil.createKey(LOCATION_PREFIX, sessionId);
    redisUtil.saveObjectList(key, locationHistoryRequest, LOCATION_EXPIRATION);
  }

  public List<LocationHistoryRequest> getLocationHistories(String sessionId) {
    String key = redisUtil.createKey(LOCATION_PREFIX, sessionId);
    return redisUtil.getObjectList(key, LocationHistoryRequest.class);
  }

  private void validateLocationData(LocationHistoryRequest locationRequest) {
    if (locationRequest.getLatitude() < -90 || locationRequest.getLatitude() > 90) {
      throw new IllegalArgumentException("Invalid latitude value");
    }
    if (locationRequest.getLongitude() < -180 || locationRequest.getLongitude() > 180) {
      throw new IllegalArgumentException("Invalid longitude value");
    }
    if (locationRequest.getTimestamp() == null) {
      throw new IllegalArgumentException("Timestamp cannot be null");
    }
  }
}
