package cl.gbarrera.demo.controller;

import static cl.gbarrera.demo.util.Messages.VALIDATION_FAILED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import cl.gbarrera.demo.product.application.dto.ProductDTO;
import cl.gbarrera.demo.product.application.dto.ProductRequestDTO;
import cl.gbarrera.demo.product.application.dto.ProductSearchCriteria;
import cl.gbarrera.demo.product.application.service.ProductService;
import cl.gbarrera.demo.product.domain.Product;
import cl.gbarrera.demo.product.infrastructure.web.controller.ProductController;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

  @Mock private ProductService productService;

  @Mock private BindingResult bindingResult;

  @InjectMocks private ProductController controller;

  private ProductDTO sampleDto;
  private ProductRequestDTO sampleRequest;
  private ProductSearchCriteria sampleCriteria;
  private Page<Product> samplePage;

  @BeforeEach
  void setUp() {
    sampleDto = new ProductDTO(1L, "Sample", 100L);
    sampleRequest = new ProductRequestDTO("Sample", 100L);
    sampleCriteria = new ProductSearchCriteria();
    sampleCriteria.setName("Sample");
    sampleCriteria.setPage(0);
    sampleCriteria.setSize(10);
    samplePage =
        new PageImpl<>(
            List.of(new Product(1L, "SampleA", 100L, 0), new Product(2L, "SampleB", 200L, 0)));
  }

  @Test
  void getAll_shouldReturnList() {
    when(productService.getAllProducts()).thenReturn(List.of(sampleDto));

    ResponseEntity<List<ProductDTO>> resp = controller.getAll();

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertNotNull(resp.getBody());
    assertEquals(1, resp.getBody().size());
    assertEquals("Sample", resp.getBody().get(0).getName());
  }

  @Test
  void getById_shouldReturnDto() {
    when(productService.getProductById(1L)).thenReturn(sampleDto);

    ResponseEntity<ProductDTO> resp = controller.getById(1L);

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertEquals(sampleDto, resp.getBody());
  }

  @Test
  void createProduct_shouldReturnCreated_whenValid() {
    when(bindingResult.hasErrors()).thenReturn(false);
    when(productService.createProduct(sampleRequest)).thenReturn(sampleDto);

    ResponseEntity<?> resp = controller.createProduct(sampleRequest, bindingResult);

    assertEquals(HttpStatus.CREATED, resp.getStatusCode());
    assertTrue(resp.getBody() instanceof ProductDTO);
    assertEquals(sampleDto, resp.getBody());
  }

  @Test
  void createProduct_shouldReturnBadRequest_whenInvalid() {
    when(bindingResult.hasErrors()).thenReturn(true);
    when(bindingResult.getAllErrors())
        .thenReturn(List.of(new ObjectError("name", "Name required")));

    ResponseEntity<?> resp = controller.createProduct(sampleRequest, bindingResult);

    assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) resp.getBody();
    assertEquals(VALIDATION_FAILED, body.get("error"));
    assertEquals(400, body.get("status"));
    assertTrue(((List<?>) body.get("messages")).contains("Name required"));
  }

  @Test
  void updateProduct_shouldReturnOk_whenValid() {
    when(bindingResult.hasErrors()).thenReturn(false);
    when(productService.updateProduct(eq(1L), eq(sampleRequest))).thenReturn(sampleDto);

    ResponseEntity<?> resp = controller.updateProduct(1L, sampleRequest, bindingResult);

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertEquals(sampleDto, resp.getBody());
  }

  @Test
  void updateProduct_shouldReturnBadRequest_whenInvalid() {
    when(bindingResult.hasErrors()).thenReturn(true);
    when(bindingResult.getAllErrors())
        .thenReturn(List.of(new ObjectError("price", "Price must be positive")));

    ResponseEntity<?> resp = controller.updateProduct(1L, sampleRequest, bindingResult);

    assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) resp.getBody();
    assertEquals(VALIDATION_FAILED, body.get("error"));
    assertTrue(((List<?>) body.get("messages")).contains("Price must be positive"));
  }

  @Test
  void deleteProduct_shouldReturnNoContent() {
    doNothing().when(productService).deleteProduct(1L);

    ResponseEntity<?> resp = controller.deleteProduct(1L);

    assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
    assertNull(resp.getBody());
    verify(productService).deleteProduct(1L);
  }

  @Test
  void testSearchProducts() {
    when(productService.search(any(ProductSearchCriteria.class))).thenReturn(samplePage);

    ResponseEntity<Page<ProductDTO>> resp = controller.search(sampleCriteria);

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    Page<ProductDTO> dtos = resp.getBody();
    assertNotNull(dtos);
    assertEquals(2, dtos.getContent().size());
    List<String> names =
        dtos.getContent().stream().map(ProductDTO::getName).collect(Collectors.toList());
    assertEquals(List.of("SampleA", "SampleB"), names);
  }
}
