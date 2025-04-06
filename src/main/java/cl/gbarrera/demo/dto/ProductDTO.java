package cl.gbarrera.demo.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private Integer price;
}
