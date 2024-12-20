package org.example.hatealcohol.gps.config.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.example.hatealcohol.gps.exception.InvalidUriException;
import org.example.hatealcohol.gps.exception.SocketIdNullException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final ConcurrentMap<String, WebSocketSession> GPS_SESSION = new ConcurrentHashMap<>();

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    super.afterConnectionEstablished(session);
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
