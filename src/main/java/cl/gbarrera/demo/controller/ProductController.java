package cl.gbarrera.demo.controller;

import cl.gbarrera.demo.dto.ProductDTO;
import cl.gbarrera.demo.dto.ProductRequestDTO;
import cl.gbarrera.demo.dto.ProductSearchCriteria;
import cl.gbarrera.demo.model.Product;
import cl.gbarrera.demo.service.ProductService;
import cl.gbarrera.demo.service.impl.ProductServiceImpl;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cl.gbarrera.demo.util.Messages.VALIDATION_FAILED;


@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAll() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getById(@PathVariable Long id) {
        ProductDTO dto = productService.getProductById(id);

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequestDTO productRequestDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", VALIDATION_FAILED);
            errorResponse.put("messages", errors);
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());

            return ResponseEntity.badRequest().body(errorResponse);
        }

        ProductDTO createdProduct = productService.createProduct(productRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO productRequestDTO,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", VALIDATION_FAILED);
            errorResponse.put("messages", errors);
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());

            return ResponseEntity.badRequest().body(errorResponse);
        }

        ProductDTO updatedProduct = productService.updateProduct(id, productRequestDTO);

        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/search")
    public ResponseEntity<Page<ProductDTO>> search(@RequestBody ProductSearchCriteria criteria) {
        Page<Product> products = productService.search(criteria);

        List<ProductDTO> dtos = products.stream()
                .map(p -> new ProductDTO(p.getId(), p.getName(), p.getPrice()))
                .collect(Collectors.toList());

        Page<ProductDTO> dtoPage = new PageImpl<>(
                dtos,
                products.getPageable(),
                products.getTotalElements()
        );

        return ResponseEntity.ok(dtoPage);
    }
}
