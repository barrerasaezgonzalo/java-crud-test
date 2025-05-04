package cl.gbarrera.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductDTO {

  private Long id;

  @NotNull() private String name;

  @NotNull() @PositiveOrZero() private Integer price;
}
