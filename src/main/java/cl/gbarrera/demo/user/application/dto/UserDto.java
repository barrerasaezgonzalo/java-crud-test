package cl.gbarrera.demo.user.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
  @JsonIgnore
  private Long id;

  @NotNull() private String username;

  @NotNull() private String password;
}
