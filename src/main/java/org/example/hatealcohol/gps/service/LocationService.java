package org.example.hatealcohol.gps.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.hatealcohol.common.redis.RedisUtil;
import org.example.hatealcohol.gps.dto.LocationRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationService {

  private final RedisUtil redisUtil;
  private final long LOCATION_EXPIRATION = 172800; // 2일
  private final String LOCATION_PREFIX = "location_history";

  public void saveLocationList(String sessionId, LocationRequest locationRequest) {

    validateLocationData(locationRequest);
    String key = redisUtil.createKey(LOCATION_PREFIX, sessionId);
    redisUtil.saveObjectList(key, locationRequest, LOCATION_EXPIRATION);
  }

  public List<LocationRequest> getLocationHistories(String sessionId) {
    String key = redisUtil.createKey(LOCATION_PREFIX, sessionId);
    return redisUtil.getObjectList(key, LocationRequest.class);
  }

  private void validateLocationData(LocationRequest locationRequest) {
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
