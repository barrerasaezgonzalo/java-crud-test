package cl.gbarrera.demo.product.application.dto;

import static cl.gbarrera.demo.util.Messages.REQUEST_INVALID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

  @NotNull(message = REQUEST_INVALID)
  private String name;

  @NotNull(message = REQUEST_INVALID)
  @PositiveOrZero
  private Long price;
}
