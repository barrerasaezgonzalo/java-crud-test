package cl.gbarrera.demo.product.application.mapper;

import cl.gbarrera.demo.product.domain.Product;
import cl.gbarrera.demo.product.application.dto.ProductRequestDTO;
import cl.gbarrera.demo.product.application.dto.ProductResponseDTO;

public class ProductMapper {

    private ProductMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Product toEntity(ProductRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        Product product = new Product();
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        return product;
    }

   public static ProductResponseDTO toDto(Product product) {
        if (product == null) {
            return null;
        }
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        return dto;
    }
}
