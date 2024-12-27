package org.example.hatealcohol.gps.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;

@Entity
@Getter
@Table(name = "sharing_sessions")
public class SharingSession { // 사용자의 위치를 공유하는 세션을 관리 및 추적을 위한 테이블

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 관계 매핑 안 함
  @Column(name = "schedule_id", nullable = false)
  private Long scheduleId;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  @Column(name = "started_at", nullable = false)
  private LocalDateTime startedAt;

  @Column(name = "ended_at")
  private LocalDateTime endedAt;

  @OneToOne(mappedBy = "session", cascade = CascadeType.ALL)
  private PathImage pathImage;
}
