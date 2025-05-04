package cl.gbarrera.demo.exception;

import cl.gbarrera.demo.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(InvalidProductException.class)
  public ResponseEntity<ErrorResponse> handleProductNotFoundException(
      InvalidProductException ex, HttpServletRequest request) {
    String requestId = UUID.randomUUID().toString();
    ErrorResponse errorResponse =
        new ErrorResponse(
            ex.getStatus().value(),
            ex.getMessage(),
            request.getRequestURI(),
            requestId,
            LocalDateTime.now());
    return new ResponseEntity<>(errorResponse, ex.getStatus());
  }
}
