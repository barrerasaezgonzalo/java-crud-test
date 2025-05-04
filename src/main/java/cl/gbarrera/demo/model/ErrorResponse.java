package cl.gbarrera.demo.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ErrorResponse {
  private int status;
  private String error;
  private String path;
  private String requestId;
  private LocalDateTime timestamp;
}
