package cl.gbarrera.demo.service;

import cl.gbarrera.demo.dto.PagedResponse;
import cl.gbarrera.demo.dto.ProductDTO;
import cl.gbarrera.demo.dto.ProductRequestDTO;
import cl.gbarrera.demo.dto.ProductSearchCriteria;
import cl.gbarrera.demo.exception.InvalidProductException;
import cl.gbarrera.demo.model.Product;
import cl.gbarrera.demo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static cl.gbarrera.demo.util.Messages.PRODUCT_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllProducts() {

        List<Product> mockProducts = Arrays.asList(
                new Product(1L, "Product 1", 100L),
                new Product(2L, "Product 2", 200L)
        );
        when(productRepository.findAll()).thenReturn(mockProducts);
        List<ProductDTO> products = productService.getAllProducts();
        assertEquals(2, products.size());
        assertEquals("Product 1", products.get(0).getName());
        assertEquals(100, products.get(0).getPrice());
    }

    @Test
    public void testGetProductById() {
        ProductRepository mockRepo = Mockito.mock(ProductRepository.class);
        ProductService service = new ProductService(mockRepo);
        Product product = new Product(1L, "Producto", 1000L);
        Mockito.when(mockRepo.findById(1L)).thenReturn(Optional.of(product));
        ProductDTO result = service.getProductById(1L);
        assertEquals("Producto", result.getName());
    }

    @Test
    void testGetProductById_NotFound_ThrowsInvalidProductException() {
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        InvalidProductException exception = assertThrows(InvalidProductException.class, () -> {
            productService.getProductById(productId);
        });
        assert(exception.getStatus() == HttpStatus.NOT_FOUND);
        assert(exception.getMessage().contains(PRODUCT_NOT_FOUND));
    }

    @Test
    void testCreateProduct_Success() {
        ProductRequestDTO requestDTO = new ProductRequestDTO();
        requestDTO.setName("Producto de prueba");
        requestDTO.setPrice((long) 1500.0);
        Product savedProduct = new Product();
        savedProduct.setId(1L);
        savedProduct.setName(requestDTO.getName());
        savedProduct.setPrice(requestDTO.getPrice());
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        ProductDTO result = productService.createProduct(requestDTO);
        assertEquals(1L, result.getId());
        assertEquals("Producto de prueba", result.getName());
        assertEquals(1500.0, result.getPrice(), 0.001);

    }

    @Test
    void updateProduct_existingProduct_updatesAndReturnsDTO() {
        Long id = 1L;
        Product existingProduct = new Product();
        existingProduct.setId(id);
        existingProduct.setName("Old Name");
        existingProduct.setPrice((long) 1000.0);
        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setName("New Name");
        dto.setPrice((long) 1500.0);
        Product updatedProduct = new Product();
        updatedProduct.setId(id);
        updatedProduct.setName(dto.getName());
        updatedProduct.setPrice(dto.getPrice());
        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(existingProduct)).thenReturn(updatedProduct);
        ProductDTO result = productService.updateProduct(id, dto);
        assertEquals(id, result.getId());
        assertEquals("New Name", result.getName());
        assertEquals(1500.0, result.getPrice(), 0.001);
        verify(productRepository).findById(id);
        verify(productRepository).save(existingProduct);
    }

    @Test
    void updateProduct_productNotFound_throwsInvalidProductException() {
        Long id = 1L;
        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setName("Name");
        dto.setPrice((long) 1500.0);
        when(productRepository.findById(id)).thenReturn(Optional.empty());
        InvalidProductException exception = assertThrows(InvalidProductException.class, () -> {
            productService.updateProduct(id, dto);
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(productRepository).findById(id);
        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_existingId_deletesProduct() {
        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);
        product.setName("Test Product");
        product.setPrice((long) 1000.0);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);
        productService.deleteProduct(productId);
        verify(productRepository).findById(productId);
        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_nonExistingId_throwsInvalidProductException() {
        Long productId = 99L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        InvalidProductException exception = assertThrows(InvalidProductException.class, () -> {
            productService.deleteProduct(productId);
        });
        assertEquals(PRODUCT_NOT_FOUND, exception.getMessage());
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void testSearchReturnsPagedResponse() {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setPage(0);
        criteria.setSize(2);
        criteria.setSortBy("name");
        criteria.setSortDir("ASC");
        Product product1 = new Product(1L, "Prod1", (long) 100.0);
        Product product2 = new Product(2L, "Prod2", (long) 200.0);

        List<Product> productList = List.of(product1, product2);
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), Sort.by(Sort.Direction.ASC, "name"));
        Page<Product> productPage = new PageImpl<>(productList, pageable, 10);

        when(productRepository.findAll((Specification<Product>) any(), any(Pageable.class)))
                .thenReturn(productPage);
        PagedResponse<Product> response = productService.search(criteria);
        assertEquals(2, response.getResults().size());
        assertEquals(0, response.getPage());
        assertEquals(2, response.getSize());
        assertEquals(10, response.getTotalElements());
        assertEquals(5, response.getTotalPages());
        assertEquals(false, response.isLast());
    }
}
