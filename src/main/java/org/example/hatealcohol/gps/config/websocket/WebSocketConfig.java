package org.example.hatealcohol.gps.config.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

  private final WebSocketHandler webSocketHandler;

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

    // ws://localhost:8080/gps?role={}&userId={} -> 호스트
    // ws://localhost:8080/gps?role={}&userId={}&hosSessionId={} -> 공유자
    registry.addHandler(webSocketHandler, "/gps")
        .setAllowedOrigins("*");
  }
}
