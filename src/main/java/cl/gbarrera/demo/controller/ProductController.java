package cl.gbarrera.demo.controller;

import cl.gbarrera.demo.dto.ProductDTO;
import cl.gbarrera.demo.model.Product;
import cl.gbarrera.demo.repository.ProductRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {

  private final ProductRepository repository;

  public ProductController(ProductRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public List<ProductDTO> getAll() {
    return repository.findAll().stream()
        .map(p -> new ProductDTO(p.getId(), p.getName(), p.getPrice()))
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProductDTO> getById(@PathVariable Long id) {
    return repository
        .findById(id)
        .map(p -> new ProductDTO(p.getId(), p.getName(), p.getPrice()))
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<ProductDTO> create(@Valid @RequestBody ProductDTO dto) {
    Product product = new Product();
    product.setName(dto.getName());
    product.setPrice(dto.getPrice());
    Product saved = repository.save(product);
    return ResponseEntity.ok(new ProductDTO(saved.getId(), saved.getName(), saved.getPrice()));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProductDTO> update(
      @PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
    return repository
        .findById(id)
        .map(
            existing -> {
              existing.setName(dto.getName());
              existing.setPrice(dto.getPrice());
              Product updated = repository.save(existing);
              return ResponseEntity.ok(
                  new ProductDTO(updated.getId(), updated.getName(), updated.getPrice()));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    if (!repository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }
    repository.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
