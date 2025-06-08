package cl.gbarrera.demo.product.application.mapper;

import cl.gbarrera.demo.product.application.dto.ProductRequestDTO;
import cl.gbarrera.demo.product.application.dto.ProductResponseDTO;
import cl.gbarrera.demo.product.domain.Product;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

public class ProductMapperTest {

    @Test
    void privateConstructor_ShouldThrowException() throws Exception {
        Constructor<ProductMapper> constructor = ProductMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> constructor.newInstance());
        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
    }

    @Test
    void toEntity_withValidDto_returnsProduct() {
        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setName("TestProduct");
        dto.setPrice(1000L);

        Product product = ProductMapper.toEntity(dto);

        assertNotNull(product);
        assertEquals("TestProduct", product.getName());
        assertEquals(1000L, product.getPrice());
    }

    @Test
    void toDto_withValidProduct_returnsDto() {
        Product product = new Product();
        product.setId(1L);
        product.setName("TestProduct");
        product.setPrice(1000L);

        ProductResponseDTO dto = ProductMapper.toDto(product);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("TestProduct", dto.getName());
        assertEquals(1000L, dto.getPrice());
    }

    @Test
    void toEntity_ShouldReturnNull_WhenDtoIsNull() {
        Product product = ProductMapper.toEntity(null);
        assertNull(product);
    }

    @Test
    void toDto_ShouldReturnNull_WhenProductIsNull() {
        ProductResponseDTO dto = ProductMapper.toDto(null);
        assertNull(dto);
    }


}
