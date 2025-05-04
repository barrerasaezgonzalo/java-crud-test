package cl.gbarrera.demo.service;

import static cl.gbarrera.demo.util.Messages.*;

import cl.gbarrera.demo.dto.ProductDTO;
import cl.gbarrera.demo.exception.InvalidProductException;
import cl.gbarrera.demo.model.Product;
import cl.gbarrera.demo.repository.ProductRepository;
import java.util.*;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductService {

  private final ProductRepository productRepository;

  public ProductService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  public Flux<ProductDTO> getAllProducts() {
    List<Product> products = productRepository.findAll();

    if (products.isEmpty()) {
      return Flux.empty();
    }

    return Flux.fromIterable(products)
        .map(product -> new ProductDTO(product.getId(), product.getName(), product.getPrice()));
  }

  public Mono<ProductDTO> getProductById(Long id) {
    return Mono.justOrEmpty(productRepository.findById(id))
        .map(product -> new ProductDTO(product.getId(), product.getName(), product.getPrice()))
        .switchIfEmpty(
            Mono.error(
                new InvalidProductException(PRODUCT_ID_NOT_FOUND, HttpStatus.NOT_FOUND, id)));
  }
  ;

  public Mono<ProductDTO> createProduct(ProductDTO productDTO) {

    if (productDTO.getName() == null || productDTO.getName().isEmpty()) {
      return Mono.error(
          new InvalidProductException(PRODUCT_NAME_CANNOT_BE_NULL, HttpStatus.BAD_REQUEST));
    }
    if (productDTO.getPrice() == null || productDTO.getPrice() < 0) {
      return Mono.error(
          new InvalidProductException(PRODUCT_PRICE_CANNOT_BE_NULL, HttpStatus.BAD_REQUEST));
    }

    Product product = new Product(productDTO.getId(), productDTO.getName(), productDTO.getPrice());
    return Mono.fromCallable(() -> productRepository.save(product))
        .map(
            savedProduct ->
                new ProductDTO(
                    savedProduct.getId(), savedProduct.getName(), savedProduct.getPrice()));
  }

  public Mono<ProductDTO> updateProduct(Long id, ProductDTO productDTO) {

    if (productDTO.getName() == null || productDTO.getName().isEmpty()) {
      return Mono.error(
          new InvalidProductException(PRODUCT_NAME_CANNOT_BE_NULL, HttpStatus.BAD_REQUEST));
    }
    if (productDTO.getPrice() == null || productDTO.getPrice() <= 0) {
      return Mono.error(
          new InvalidProductException(PRODUCT_PRICE_CANNOT_BE_NULL, HttpStatus.BAD_REQUEST));
    }

    return Mono.justOrEmpty(productRepository.findById(id))
        .flatMap(
            existingProduct -> {
              existingProduct.setName(productDTO.getName());
              existingProduct.setPrice(productDTO.getPrice());
              return Mono.just(productRepository.save(existingProduct))
                  .map(
                      savedProduct ->
                          new ProductDTO(
                              savedProduct.getId(),
                              savedProduct.getName(),
                              savedProduct.getPrice()));
            })
        .switchIfEmpty(
            Mono.error(
                new InvalidProductException(
                    String.format(PRODUCT_ID_NOT_FOUND, id), HttpStatus.NOT_FOUND)));
  }

  public Mono<Void> deleteProduct(Long id) {
    return Mono.justOrEmpty(productRepository.findById(id))
        .switchIfEmpty(
            Mono.error(
                new InvalidProductException(
                    String.format(PRODUCT_ID_NOT_FOUND, id), HttpStatus.NOT_FOUND)))
        .flatMap(
            product -> {
              productRepository.delete(product);
              return Mono.empty();
            });
  }

  public Flux<ProductDTO> searchAndValidate(Map<String, String> queryParams) {
    Set<String> validParams = Set.of("name", "minPrice", "maxPrice");
    for (String key : queryParams.keySet()) {
      if (!validParams.contains(key)) {
        throw new InvalidProductException(INVALID_PARAMETER + ": " + key, HttpStatus.BAD_REQUEST);
      }
    }

    String name = queryParams.get("name");
    String minPriceRaw = queryParams.get("minPrice");
    String maxPriceRaw = queryParams.get("maxPrice");

    if (name != null && name.isEmpty()) {
      throw new InvalidProductException(PRODUCT_NAME_CANNOT_BE_EMPTY, HttpStatus.BAD_REQUEST);
    }

    if ((minPriceRaw != null && minPriceRaw.isEmpty())
        || (maxPriceRaw != null && maxPriceRaw.isEmpty())) {
      throw new InvalidProductException(PRODUCT_PRICE_CANNOT_BE_NULL, HttpStatus.BAD_REQUEST);
    }

    Double minPrice = null;
    Double maxPrice = null;

    try {
      if (minPriceRaw != null) {
        minPrice = Double.valueOf(minPriceRaw);
      }
      if (maxPriceRaw != null) {
        maxPrice = Double.valueOf(maxPriceRaw);
      }
    } catch (NumberFormatException e) {
      throw new InvalidProductException(PRODUCT_PRICE_CANNOT_BE_NULL, HttpStatus.BAD_REQUEST);
    }

    if (minPrice != null && minPrice < 0) {
      throw new InvalidProductException(PRODUCT_PRICE_CANNOT_BE_NULL, HttpStatus.BAD_REQUEST);
    }

    final Double finalMinPrice = minPrice;
    final Double finalMaxPrice = maxPrice;

    Stream<Product> filtered = productRepository.findAll().stream();

    if (name != null) {
      filtered = filtered.filter(p -> p.getName().toLowerCase().contains(name.toLowerCase()));
    }
    if (finalMinPrice != null) {
      filtered = filtered.filter(p -> p.getPrice() >= finalMinPrice);
    }
    if (finalMaxPrice != null) {
      filtered = filtered.filter(p -> p.getPrice() <= finalMaxPrice);
    }

    return Flux.fromStream(filtered)
        .map(product -> new ProductDTO(product.getId(), product.getName(), product.getPrice()));
  }
}
