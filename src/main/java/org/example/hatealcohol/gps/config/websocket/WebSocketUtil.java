package org.example.hatealcohol.gps.config.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArrayList;
import org.example.hatealcohol.gps.dto.LocationRequest;
import org.example.hatealcohol.gps.exception.InvalidUriException;
import org.example.hatealcohol.gps.exception.WebSocketHandleMessageException;
import org.example.hatealcohol.gps.exception.WebSocketJsonParsingException;
import org.example.hatealcohol.gps.exception.WebSocketSessionIdNullException;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class WebSocketUtil {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static void validationUri(URI uri) {

    if (uri == null) {
      throw new InvalidUriException("유효한 URI가 아닙니다");
    }
  }

  public static String extractSessionIdFromUri(String uri) {
    String sessionId = null;
    if (uri.contains("sessionId=")) {
      sessionId = URLDecoder.decode(uri.split("sessionId=")[1], StandardCharsets.UTF_8);
    }
    if (sessionId == null) {
      throw new WebSocketSessionIdNullException("세션 아이디가 없습니다.");
    }
    return sessionId;
  }

  public static String extractRoleFromUri(String uri) {
    String role = null;
    if (uri.contains("role=")) {
      role = URLDecoder.decode(uri.split("role=")[1].split("&")[0], StandardCharsets.UTF_8);
    }
    if (role == null) {
      throw new InvalidUriException("역할이 없습니다.");
    }
    return role;
  }

  public static LocationRequest toLocationRequest(String payload) {

    try {
      return objectMapper.readValue(payload, LocationRequest.class);
    } catch (Exception e) {
      throw new WebSocketJsonParsingException("웹소켓 ObjectMapper parsing 에러: " + e.getMessage());
    }
  }

  public static void sendLocationDataToSharedUser(CopyOnWriteArrayList<WebSocketSession> sharedUsers, LocationRequest locationRequest) {

    try {
      String message = serializeToJson(locationRequest);

      for (WebSocketSession session : sharedUsers) {
        if (session.isOpen()) {
          session.sendMessage(new TextMessage(message));
        }
      }
    } catch (Exception e) {
      throw new WebSocketHandleMessageException("웹소켓 메시지 처리 에러: " + e.getMessage());
    }
  }

  private static String serializeToJson(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (Exception e) {
      throw new WebSocketJsonParsingException("웹소켓 ObjectMapper JSON 직렬화 에러: " + e.getMessage());
    }
  }
}
