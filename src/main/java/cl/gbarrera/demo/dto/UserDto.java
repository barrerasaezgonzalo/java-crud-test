package cl.gbarrera.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class UserDto {

  private Long id;

  @NotNull() private String username;

  @NotNull() private String password;
}
