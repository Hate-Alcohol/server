package org.example.hatealcohol.gps.exception;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GpsExceptionHandler {

  @ExceptionHandler
  public ResponseEntity<ErrorResponse> catchRedisGetObjectListException(RedisGetObjectListException e) {
    return new ResponseEntity<>(buildErrorResponse(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ErrorResponse buildErrorResponse(String message) {
    return ErrorResponse.builder()
        .message(message)
        .build();
  }

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
