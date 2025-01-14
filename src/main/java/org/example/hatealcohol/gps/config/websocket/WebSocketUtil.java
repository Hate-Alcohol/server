package org.example.hatealcohol.gps.config.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.example.hatealcohol.gps.dto.LocationRequest;
import org.example.hatealcohol.gps.exception.InvalidUriException;
import org.example.hatealcohol.gps.exception.WebSocketHandleMessageException;
import org.example.hatealcohol.gps.exception.WebSocketJsonParsingException;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class WebSocketUtil {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static void validationUri(URI uri) {

    if (uri == null) {
      throw new InvalidUriException("유효한 URI가 아닙니다");
    }
  }

  public static Map<String, String> extractQueryParams(String uri) {

    Map<String, String> queryParams = new HashMap<>();
    String[] parts = uri.split("\\?");

    if (parts.length < 2) {
      throw new InvalidUriException("URI에 쿼리 스트링이 없습니다: " + uri);
    }

    String query = parts[1];

    for (String param : query.split("&")) {
      String[] keyValue = param.split("=");

      if (keyValue.length < 2) {
        throw new InvalidUriException("올바르지 않은 쿼리 파라미터 형식입니다: " + param);
      }

      String key = decode(keyValue[0]);
      String value = decode(keyValue[1]);

      queryParams.put(key, value);
    }

    return queryParams;
  }

  private static String decode(String value) {
    return URLDecoder.decode(value, StandardCharsets.UTF_8);
  }

  public static LocationRequest toLocationRequest(String payload) {

    try {
      return objectMapper.readValue(payload, LocationRequest.class);
    } catch (Exception e) {
      throw new WebSocketJsonParsingException("웹소켓 ObjectMapper parsing 에러: " + e.getMessage());
    }
  }

  public static void validationWebSocketSession(WebSocketSession hostSession) {
    if (hostSession == null || !hostSession.isOpen()) {
      throw new InvalidUriException("호스트 세션이 존재하지 않거나 닫혀 있습니다.");
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
