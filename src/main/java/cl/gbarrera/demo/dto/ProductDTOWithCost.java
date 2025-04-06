package cl.gbarrera.demo.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductDTOWithCost {
    private final Long id;
    private final String name;
    private final Integer price;
    private final Integer costPrice;
}
