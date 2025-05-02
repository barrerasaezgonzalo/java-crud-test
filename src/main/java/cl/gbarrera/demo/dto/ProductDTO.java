package cl.gbarrera.demo.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@AllArgsConstructor
public class ProductDTO {

    private Long id;

    @NotNull()
    private String name;

    @NotNull()
    @PositiveOrZero()
    private Integer price;

    private Integer costPrice;
}
