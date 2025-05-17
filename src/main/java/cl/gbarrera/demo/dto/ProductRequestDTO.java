package cl.gbarrera.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static cl.gbarrera.demo.util.Messages.REQUEST_INVALID;

@Getter
@Setter
@NoArgsConstructor
public class ProductRequestDTO {

    @NotNull(message = REQUEST_INVALID)
    private String name;
    @NotNull(message = REQUEST_INVALID)
    @PositiveOrZero
    private Long price;
}
