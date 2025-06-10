package cl.gbarrera.demo.product.infrastructure.web.controller;

import static cl.gbarrera.demo.util.Messages.VALIDATION_FAILED;

import cl.gbarrera.demo.product.application.dto.ProductDTO;
import cl.gbarrera.demo.product.application.dto.ProductRequestDTO;
import cl.gbarrera.demo.product.application.dto.ProductSearchCriteria;
import cl.gbarrera.demo.product.application.service.ProductService;
import cl.gbarrera.demo.product.domain.Product;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/products")
public class ProductController {

  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @SecurityRequirement(name = "bearerAuth")
  @GetMapping
  public ResponseEntity<List<ProductDTO>> getAll() {
    log.info("Fetching all products");
    List<ProductDTO> products = productService.getAllProducts();
    return ResponseEntity.ok(products);
  }

  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("/{id}")
  public ResponseEntity<ProductDTO> getById(@PathVariable Long id) {
    log.info("Fetching product with id {}", id);
    ProductDTO dto = productService.getProductById(id);
    return ResponseEntity.ok(dto);
  }

  @SecurityRequirement(name = "bearerAuth")
  @PostMapping
  public ResponseEntity<?> createProduct(
      @Valid @RequestBody ProductRequestDTO productRequestDTO, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      List<String> errors =
          bindingResult.getAllErrors().stream()
              .map(DefaultMessageSourceResolvable::getDefaultMessage)
              .toList();
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("error", VALIDATION_FAILED);
      errorResponse.put("messages", errors);
      errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
      return ResponseEntity.badRequest().body(errorResponse);
    }
    ProductDTO createdProduct = productService.createProduct(productRequestDTO);
    log.info("Created product with id {}", createdProduct.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
  }

  @SecurityRequirement(name = "bearerAuth")
  @PutMapping("/{id}")
  public ResponseEntity<?> updateProduct(
      @PathVariable Long id,
      @Valid @RequestBody ProductRequestDTO productRequestDTO,
      BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      List<String> errors =
          bindingResult.getAllErrors().stream()
              .map(DefaultMessageSourceResolvable::getDefaultMessage)
              .toList();

      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("error", VALIDATION_FAILED);
      errorResponse.put("messages", errors);
      errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
      return ResponseEntity.badRequest().body(errorResponse);
    }

    ProductDTO updatedProduct = productService.updateProduct(id, productRequestDTO);
    log.info("Updated product with id {}", id);
    return ResponseEntity.ok(updatedProduct);
  }

  @SecurityRequirement(name = "bearerAuth")
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
    productService.deleteProduct(id);
    log.info("Deleted product with id {}", id);
    return ResponseEntity.noContent().build();
  }

  @SecurityRequirement(name = "bearerAuth")
  @PostMapping("/search")
  public ResponseEntity<Page<ProductDTO>> search(@RequestBody ProductSearchCriteria criteria) {
    Page<Product> products = productService.search(criteria);

    List<ProductDTO> dtos =
        products.stream()
            .map(p -> new ProductDTO(p.getId(), p.getName(), p.getPrice()))
            .collect(Collectors.toList());

    Page<ProductDTO> dtoPage =
        new PageImpl<>(dtos, products.getPageable(), products.getTotalElements());

    return ResponseEntity.ok(dtoPage);
  }
}
