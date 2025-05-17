package cl.gbarrera.demo.service;

import static cl.gbarrera.demo.util.Messages.*;
import cl.gbarrera.demo.dto.ProductDTO;
import cl.gbarrera.demo.dto.ProductRequestDTO;
import cl.gbarrera.demo.exception.InvalidProductException;
import cl.gbarrera.demo.model.Product;
import cl.gbarrera.demo.repository.ProductRepository;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductDTO> getAllProducts() {

        return productRepository.findAll().stream()
                .map(p -> new ProductDTO(p.getId(), p.getName(), p.getPrice()))
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {

        return productRepository.findById(id)
                .map(p -> new ProductDTO(p.getId(), p.getName(), p.getPrice()))
                .orElseThrow(() -> new InvalidProductException(
                        String.format(PRODUCT_NOT_FOUND),
                        HttpStatus.NOT_FOUND
                ));
    }

    ;

    public ProductDTO createProduct(@Valid ProductRequestDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        Product saved = productRepository.save(product);

        return new ProductDTO(saved.getId(), saved.getName(), saved.getPrice());
    }

    public ProductDTO updateProduct(Long id, @Valid ProductRequestDTO dto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new InvalidProductException(PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND));

        existingProduct.setName(dto.getName());
        existingProduct.setPrice(dto.getPrice());

        Product updated = productRepository.save(existingProduct);

        return new ProductDTO(updated.getId(), updated.getName(), updated.getPrice());
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new InvalidProductException(PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND));

        productRepository.delete(product);
    }
}
