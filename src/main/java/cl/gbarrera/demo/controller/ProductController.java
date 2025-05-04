package cl.gbarrera.demo.controller;

import cl.gbarrera.demo.dto.ProductDTO;
import cl.gbarrera.demo.exception.InvalidProductException;
import cl.gbarrera.demo.service.ProductService;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = {"/products", "/products/"})
public class ProductController {

  @Autowired private ProductService productService;

  @GetMapping
  public Flux<ProductDTO> getAllProducts() {
    return productService
        .getAllProducts()
        .map(product -> new ProductDTO(product.getId(), product.getName(), product.getPrice()));
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<ProductDTO>> getProductById(@PathVariable Long id) {
    return productService
        .getProductById(id)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
    return productService
        .createProduct(productDTO)
        .map(product -> new ProductDTO(product.getId(), product.getName(), product.getPrice()));
  }

  @PutMapping("/{id}")
  public Mono<ResponseEntity<ProductDTO>> updateProduct(
      @PathVariable Long id, @RequestBody ProductDTO productDTO) {
    return productService
        .updateProduct(id, productDTO)
        .map(product -> new ProductDTO(product.getId(), product.getName(), product.getPrice()))
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public Mono<ResponseEntity<Object>> deleteProduct(@PathVariable Long id) {
    return productService.deleteProduct(id).then(Mono.just(ResponseEntity.noContent().build()));
  }

  @GetMapping("/search")
  public Mono<ResponseEntity<List<ProductDTO>>> searchProducts(
      @RequestParam Map<String, String> queryParams) {
    return productService
        .searchAndValidate(queryParams)
        .collectList()
        .map(ResponseEntity::ok)
        .onErrorResume(
            InvalidProductException.class,
            ex -> Mono.just(ResponseEntity.status(ex.getStatus()).body(Collections.emptyList())));
  }
}
