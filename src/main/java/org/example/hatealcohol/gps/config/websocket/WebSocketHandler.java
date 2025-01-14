package org.example.hatealcohol.gps.config.websocket;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.RequiredArgsConstructor;
import org.example.hatealcohol.gps.dto.LocationRequest;
import org.example.hatealcohol.gps.exception.InvalidUriException;
import org.example.hatealcohol.gps.exception.WebSocketSessionIdNullException;
import org.example.hatealcohol.gps.service.LocationService;
import org.example.hatealcohol.user.entity.User;
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

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {

    // TODO: 2024-12-20 사용자가 이머전시가 발생하면 공유 세션 테이블이 활성화 되고
    validationUri(session.getUri());
    String uri = session.getUri().toString();
    Map<String, String> queryParams = extractQueryParams(uri);

    String role = queryParams.get("role");
    String userId = queryParams.get("userId");

    if ("host".equals(role)) { // 호스트 세션 등록
      handleHost(session, queryParams, userId);

    } else if ("shared".equals(role)) {
      handleShared(session, queryParams, userId);

    } else {
      throw new InvalidUriException("알 수 없는 역할(role)입니다.: " + role);
    }
  }

  private void handleHost(WebSocketSession session, Map<String, String> queryParams, String userId) {

    String hostSessionId = session.getId();
    HOST_SESSIONS.put(hostSessionId, session);
    SHARED_SESSIONS.putIfAbsent(hostSessionId, new CopyOnWriteArrayList<>());

    session.getAttributes().put("userId", userId);

    // TODO: 공유자들에게 알림 전송
    // notifySharedUsers(hostSessionId);
  }

  private void handleShared(WebSocketSession session, Map<String, String> queryParams, String userId) throws IOException {

    String hostSessionId = queryParams.get("hostSessionId");

    if (hostSessionId == null) {
      session.close(CloseStatus.BAD_DATA);
      throw new WebSocketSessionIdNullException("세션 아이디가 없습니다.");
    }

    WebSocketSession hostSession = HOST_SESSIONS.get(hostSessionId);
    validationWebSocketSession(hostSession);
    session.getAttributes().put("hostSessionId", hostSessionId);

    // 공유자의 세션 등록
    SHARED_SESSIONS.computeIfPresent(hostSessionId, (key, sessions) -> {
      sessions.add(session);
      return sessions;
    });

    // 공유자가 들어 오면 들어왔다고 호스트에게 공유자 객체를 넘기는 로직
    User user = userService.getUserInfo(userId);
    hostSession.sendMessage(new TextMessage(user.getName()));
  }

  // 이 메서드를 통해 실시간 위치를 보내서 표현
  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) {

    String hostSessionId = session.getId();
    String payload = message.getPayload();
    String hostUserId = (String) session.getAttributes().get("userId");

    LocationRequest locationRequest = toLocationRequest(payload);

    // TODO: 2025-01-13 Redis 위치 기록 저장
    locationService.saveLocationList(hostUserId, locationRequest);

    // 호스트가 공유자들에게 위치 데이터를 보내는 로직
    CopyOnWriteArrayList<WebSocketSession> sharedUsers = SHARED_SESSIONS.get(hostSessionId);
    sendLocationDataToSharedUser(sharedUsers, locationRequest);
  }

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
    } else {

      String hostSessionId = (String) session.getAttributes().get("hostSessionId");

      if (hostSessionId != null) {
        CopyOnWriteArrayList<WebSocketSession> sharedSession = SHARED_SESSIONS.get(hostSessionId);

        if (sharedSession != null) {
          sharedSession.remove(session);
        }
      }
    }

    super.afterConnectionClosed(session, status);
  }
}
