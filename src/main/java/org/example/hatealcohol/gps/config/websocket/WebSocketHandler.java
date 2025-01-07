package org.example.hatealcohol.gps.config.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.RequiredArgsConstructor;
import org.example.hatealcohol.gps.exception.InvalidUriException;
import org.example.hatealcohol.gps.service.LocationService;
import org.example.hatealcohol.user.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import static org.example.hatealcohol.gps.config.websocket.WebSocketUtil.*;

@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

  // TODO: 2024-12-27 웹소켓 로직 설계, 프론트엔드도 해야함, 알림도 고려...?

  // 호스트(위치가 공유되는 사람) 세션과 공유자 세션을 관리하기 위한 맵
  // 아직 세션 아이디에 관련해서 어떻게 설계할지 결정을 못함
  private final ConcurrentMap<String, WebSocketSession> HOST_SESSIONS = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, CopyOnWriteArrayList<WebSocketSession>> SHARED_SESSIONS = new ConcurrentHashMap<>();
  private final LocationService locationService;
  private final UserService userService;
  private final ObjectMapper objectMapper;

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {

    // TODO: 2024-12-20 사용자가 이머전시가 발생하면 공유 세션 테이블이 활성화 되고
    // 실시간 공유가 가능해야 함
    // 이 과정에서 사용자가 설정한 사람들에게 웹소켓 세션 정보를 전해줄 수 있어야 함
    // 타 사용자들은 알림을 받고, 해당하는 세션을 통해 위치를 공유 받아야 함
    // 그와 동시에 위치를 Redis 에 기록해야 함
    validationUri(session.getUri());
    String uri = session.getUri().toString();
    String role = extractRoleFromUri(uri); // URI에서 role=host 또는 role=shared 추출

    if ("host".equals(role)) { // 호스트 세션 등록
      String hostSessionId = session.getId();
      HOST_SESSIONS.put(hostSessionId, session);
      SHARED_SESSIONS.putIfAbsent(hostSessionId, new CopyOnWriteArrayList<>());
      // notifySharedUsers(hostSessionId); // 공유자들에게 알림 전송
    } else if ("shared".equals(role)) {

      String hostSessionId = extractSessionIdFromUri(uri);
      WebSocketSession hostSession = HOST_SESSIONS.get(hostSessionId);

      if (hostSession == null || !hostSession.isOpen()) {
        throw new InvalidUriException("호스트 세션이 존재하지 않거나 닫혀 있습니다.");
      }

      session.getAttributes().put("hostSessionId", hostSessionId);

      // 공유자의 세션 등록
      SHARED_SESSIONS.computeIfPresent(hostSessionId, (key, sessions) -> {
        sessions.add(session);
        return sessions;
      });

      // 공유자가 들어 오면 들어왔다고 호스트에게 공유자 객체를 넘기는 로직
      // 영통 가능하면 재밌을듯 ㅋㅋ
    }
  }

  // 이 메서드를 통해 실시간 위치를 보내서 표현
  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    String hostSessionId = session.getId();

    // 호스트가 공유자들에게 위치 데이터를 보내는 로직
//    if (HOST_SESSIONS.containsKey(hostSessionId))
  }

  // 이머전시가 발생한 유저를 호스트라고 지칭한다면,
  // 호스트가 연결을 종료하면 전부 끝
  // but, 공유자들은 나갔다가 들어올 수 있도록 설계할 생각
  // 호스트가 나간다면 그동안의 위치 기록을 시각화 하는 로직이 실행되어야 함
  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

    String sessionId = session.getId();

    if (HOST_SESSIONS.containsKey(sessionId)) {

      List<WebSocketSession> sharedSessions = SHARED_SESSIONS.remove(sessionId);

      if (sharedSessions != null) {
        for (WebSocketSession sharedSession : sharedSessions) {
          if (sharedSession.isOpen()) {
            sharedSession.close();
          }
        }
      }

      HOST_SESSIONS.remove(sessionId);

//      // 위치 데이터 정리 및 시각화
//      String locationData = redisUtil.getLocationData(sessionId);
//      if (locationData != null) {
//        visualizeLocationData(locationData); // 위치 데이터 시각화 로직 호출
//      }
//      redisUtil.deleteLocationData(sessionId); // Redis에서 위치 데이터 제거
    } else {

      String hostSessionId = (String) session.getAttributes().get("hostSessionId");

      if (hostSessionId != null) {
        CopyOnWriteArrayList<WebSocketSession> sharedSession = SHARED_SESSIONS.get(hostSessionId);

        if (sharedSession != null) {
          sharedSession.remove(session);
        }
      }

//      SHARED_SESSIONS.forEach((hostSessionId, sessions) -> {
//        sessions.removeIf(sharedSession -> sharedSession.getId().equals(sessionId));
//      });
    }

    super.afterConnectionClosed(session, status);
  }

  /**
   * 공유 대상자들에게 알림을 전송하는 메서드.
   */
//  private void notifySharedUsers(String hostId) {
//    List<String> sharedUsers = redisUtil.getSharedUsers(hostId); // Redis에서 대상자 목록 가져오기
//    sharedUsers.forEach(userId -> {
//      // 알림 전송 로직 추가 (예: Push Notification, Email 등)
//    });
//  }
}
