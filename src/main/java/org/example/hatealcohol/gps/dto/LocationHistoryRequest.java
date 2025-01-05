package org.example.hatealcohol.gps.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LocationHistoryRequest implements Serializable {

//  private Long sessionId; -> key 값으로 쓸 예정
  private Double latitude;        // 위도
  private Double longitude;       // 경도
  private LocalDateTime timestamp;// 타임스탬프

  @Builder
  public LocationHistoryRequest(Double latitude, Double longitude, LocalDateTime timestamp) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.timestamp = timestamp;
  }
}
