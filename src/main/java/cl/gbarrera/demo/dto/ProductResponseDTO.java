package cl.gbarrera.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductResponseDTO {

    private Long id;
    private String name;
    private Long price;
}
