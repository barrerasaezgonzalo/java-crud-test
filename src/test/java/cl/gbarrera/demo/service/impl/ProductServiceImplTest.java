package cl.gbarrera.demo.service.impl;

import cl.gbarrera.demo.product.application.dto.ProductDTO;
import cl.gbarrera.demo.product.application.dto.ProductRequestDTO;
import cl.gbarrera.demo.product.application.dto.ProductSearchCriteria;
import cl.gbarrera.demo.product.application.exception.InvalidProductException;
import cl.gbarrera.demo.product.domain.Product;
import cl.gbarrera.demo.product.infrastructure.persistence.ProductRepository;
import cl.gbarrera.demo.product.infrastructure.service.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static cl.gbarrera.demo.util.Messages.PRODUCT_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceImplTest {

    private ProductRepository productRepository;
    private ProductServiceImpl productService;


    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productService = new ProductServiceImpl(productRepository);
    }

    @Test
    void testGetAllProducts() {
        List<Product> products = List.of(
                new Product(1L, "Producto A", 100L,0),
                new Product(2L, "Producto B", 200L,0)
        );
        when(productRepository.findAll()).thenReturn(products);

        List<ProductDTO> result = productService.getAllProducts();

        assertEquals(2, result.size());
        assertEquals("Producto A", result.get(0).getName());
        assertEquals("Producto B", result.get(1).getName());
    }

    @Test
    void getProductById_ShouldReturnDto_WhenFound() {
        Product product = new Product(5L, "Test", 123L,0);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        ProductDTO dto = productService.getProductById(5L);

        assertNotNull(dto);
        assertEquals(5L, dto.getId());
        assertEquals("Test", dto.getName());
        assertEquals(123L, dto.getPrice());
        verify(productRepository).findById(5L);
    }

    @Test
    void getProductById_ShouldThrowInvalidProductException_WhenNotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        InvalidProductException ex = assertThrows(InvalidProductException.class,
                () -> productService.getProductById(99L)
        );
        assertEquals(PRODUCT_NOT_FOUND, ex.getMessage());
        assertEquals(404, ex.getStatus().value());
        verify(productRepository).findById(99L);
    }

    @Test
    void search_ShouldReturnPage() {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setName("X");
        criteria.setPage(0);
        criteria.setSize(5);
        List<Product> list = List.of(
                new Product(1L, "X1", 10L,0),
                new Product(2L, "X2", 20L,0)
        );
        Page<Product> page = new PageImpl<>(list, PageRequest.of(0,5), 2);
        when(productRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(page);

        Page<Product> result = productService.search(criteria);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("X1", result.getContent().get(0).getName());
        verify(productRepository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void testCreateProduct_withArgumentCaptor() {
        ProductRequestDTO dto = new ProductRequestDTO("Nuevo Producto", 999L);
        Product savedProduct = new Product(1L, "Nuevo Producto", 999L,0);

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductDTO result = productService.createProduct(dto);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product capturedProduct = productCaptor.getValue();

        assertEquals("Nuevo Producto", capturedProduct.getName());
        assertEquals(999L, capturedProduct.getPrice());

        assertNotNull(result);
        assertEquals("Nuevo Producto", result.getName());
        assertEquals(999L, result.getPrice());
    }

    @Test
    void updateProduct_ShouldReturnDto_WhenExists() {
        Long id = 20L;
        ProductRequestDTO req = new ProductRequestDTO("Updated", 555L);
        Product existing = new Product(id, "Old", 100L,0);
        Product updated = new Product(id, "Updated", 555L, 0);
        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenReturn(updated);
        ProductDTO dto = productService.updateProduct(id, req);
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals("Updated", dto.getName());
        assertEquals(555L, dto.getPrice());
        verify(productRepository).findById(id);
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product saved = captor.getValue();
        assertEquals("Updated", saved.getName());
        assertEquals(555L, saved.getPrice());
    }

    @Test
    void updateProduct_ShouldThrowInvalidProductException_WhenNotExists() {
        Long id = 21L;
        ProductRequestDTO req = new ProductRequestDTO("X", 200L);
        when(productRepository.findById(id)).thenReturn(Optional.empty());
        InvalidProductException ex = assertThrows(InvalidProductException.class,
                () -> productService.updateProduct(id, req)
        );
        assertEquals(PRODUCT_NOT_FOUND, ex.getMessage());
        verify(productRepository).findById(id);
    }


    @Test
    void deleteProduct_ShouldDelete_WhenExists() {
        Long id = 10L;
        when(productRepository.existsById(id)).thenReturn(true);
        doNothing().when(productRepository).deleteById(id);

        productService.deleteProduct(id);

        verify(productRepository).existsById(id);
        verify(productRepository).deleteById(id);
    }

    @Test
    void deleteProduct_ShouldThrowInvalidProductException_WhenNotExists() {
        Long id = 11L;
        when(productRepository.existsById(id)).thenReturn(false);

        InvalidProductException ex = assertThrows(InvalidProductException.class, () ->
                productService.deleteProduct(id)
        );
        assertEquals(PRODUCT_NOT_FOUND, ex.getMessage());
        verify(productRepository).existsById(id);
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void testSearch() {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setName("Producto");
        criteria.setPage(0);
        criteria.setSize(10);

        List<Product> products = List.of(
                new Product(1L, "Producto A", 100L,0),
                new Product(2L, "Producto B", 200L,0)
        );

        Page<Product> page = new PageImpl<>(products, PageRequest.of(0, 10), products.size());
        when(productRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        Page<Product> result = productService.search(criteria);

        assertEquals(2, result.getContent().size());
        assertEquals("Producto A", result.getContent().get(0).getName());
    }
}
