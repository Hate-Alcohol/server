package org.example.hatealcohol.gps.config.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;
import org.example.hatealcohol.gps.config.redis.RedisUtil;
import org.example.hatealcohol.gps.exception.InvalidUriException;
import org.example.hatealcohol.gps.exception.SocketIdNullException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final ConcurrentMap<String, WebSocketSession> GPS_SESSION = new ConcurrentHashMap<>();
  private final RedisUtil redisUtil;

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {

    // TODO: 2024-12-20 사용자가 이머전시가 발생하면 공유 세션 테이블이 활성화 되고
    // 실시간 공유가 가능해야 함
    // 이 과정에서 사용자가 설정한 사람들에게 웹소켓 세션 정보를 전해줄 수 있어야 함
    // 타 사용자들은 알림을 받고, 해당하는 세션을 통해 위치를 공유 받아야 함
    // 그와 동시에 위치를 Redis 에 기록해야 함
//    session.getId() -> 얘를 공유자들에게 알림으로 보내주면 될듯?
    validationUri(session.getUri());

    String uri = session.getUri().toString();
    String id = extractIdFromUri(uri);

    GPS_SESSION.put(id, session);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    super.handleTextMessage(session, message);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    super.afterConnectionClosed(session, status);
  }

  private void validationUri(URI uri) {

    if (uri == null) {
      throw new InvalidUriException("유효한 URI가 아닙니다");
    }
  }

  private String extractIdFromUri(String uri) {

    String id = null;

    if (uri.contains("id=")) {
      id = URLDecoder.decode(uri.split("id=")[1], StandardCharsets.UTF_8);
    }

    if (id == null) {
      throw new SocketIdNullException("소켓 아이디가 없습니다.");
    }

    return id;
  }
}
