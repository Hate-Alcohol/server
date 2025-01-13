package org.example.hatealcohol.gps.config.websocket;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.example.hatealcohol.gps.exception.InvalidUriException;
import org.example.hatealcohol.gps.exception.WebSocketSessionIdNullException;

public class WebSocketUtil {

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
}
