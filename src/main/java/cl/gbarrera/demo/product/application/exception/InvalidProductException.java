package cl.gbarrera.demo.product.application.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InvalidProductException extends RuntimeException {
  private final HttpStatus status;
  private final Object detail;

  public InvalidProductException(String message, HttpStatus status) {
    super(message);
    this.status = status;
    this.detail = null;
  }
}
