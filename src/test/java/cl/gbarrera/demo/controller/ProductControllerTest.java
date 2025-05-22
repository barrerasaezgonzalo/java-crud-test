package cl.gbarrera.demo.controller;

import cl.gbarrera.demo.dto.PagedResponse;
import cl.gbarrera.demo.dto.ProductDTO;
import cl.gbarrera.demo.dto.ProductRequestDTO;
import cl.gbarrera.demo.dto.ProductSearchCriteria;
import cl.gbarrera.demo.model.Product;
import cl.gbarrera.demo.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cl.gbarrera.demo.util.Messages.VALIDATION_FAILED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @InjectMocks
    private ProductController productController;

    @Mock
    private ProductService productService;

    @Mock
    private BindingResult bindingResult;

    private ProductRequestDTO validProduct;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        validProduct = new ProductRequestDTO("Product 1", 100L);
        productDTO = new ProductDTO(1L, "Product 1", 100L);
    }

    @Test
    void getAll_shouldReturnProductList() {
        when(productService.getAllProducts()).thenReturn(List.of(productDTO));

        ResponseEntity<List<ProductDTO>> response = productController.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Product 1", response.getBody().get(0).getName());
    }

    @Test
    void getById_shouldReturnProduct() {
        when(productService.getProductById(1L)).thenReturn(productDTO);

        ResponseEntity<ProductDTO> response = productController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Product 1", response.getBody().getName());
    }

    @Test
    void createProduct_shouldReturnCreated_whenValid() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(productService.createProduct(validProduct)).thenReturn(productDTO);

        ResponseEntity<?> response = productController.createProduct(validProduct, bindingResult);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertInstanceOf(ProductDTO.class, response.getBody());
        assertEquals("Product 1", ((ProductDTO) response.getBody()).getName());
    }

    @Test
    void createProduct_shouldReturnBadRequest_whenInvalid() {
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(
                List.of(new ObjectError("name", "Name is required"))
        );

        ResponseEntity<?> response = productController.createProduct(validProduct, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(VALIDATION_FAILED, body.get("error"));
        assertEquals(400, body.get("status"));
        assertTrue(((List<?>) body.get("messages")).contains("Name is required"));
    }

    @Test
    void updateProduct_shouldReturnOk_whenValid() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(productService.updateProduct(eq(1L), any())).thenReturn(productDTO);

        ResponseEntity<?> response = productController.updateProduct(1L, validProduct, bindingResult);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ProductDTO.class, response.getBody());
        assertEquals("Product 1", ((ProductDTO) response.getBody()).getName());
    }

    @Test
    void updateProduct_shouldReturnBadRequest_whenInvalid() {
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(
                List.of(new ObjectError("price", "Price must be positive"))
        );

        ResponseEntity<?> response = productController.updateProduct(1L, validProduct, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(VALIDATION_FAILED, body.get("error"));
        assertTrue(((List<?>) body.get("messages")).contains("Price must be positive"));
    }

    @Test
    void deleteProduct_shouldReturnNoContent() {
        doNothing().when(productService).deleteProduct(1L);

        ResponseEntity<?> response = productController.deleteProduct(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void search_shouldReturnPagedResponse() {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        PagedResponse<Product> pagedResponse = new PagedResponse<>(
                List.of(),
                0,
                1,
                0L,
                1,
                true
        );

        when(productService.search(criteria)).thenReturn(pagedResponse);

        ResponseEntity<PagedResponse<Product>> response = productController.search(criteria);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, Objects.requireNonNull(response.getBody()).getTotalElements());
    }
}
