package org.example.hatealcohol.gps.exception;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GpsExceptionHandler {

  @Getter
  @NoArgsConstructor
  private static class ErrorResponse {

    private LocalDateTime timestamp;
    private String message;

    @Builder
    public ErrorResponse(String message) {
      this.timestamp = LocalDateTime.now();
      this.message = message;
    }
  }
}
