package cl.gbarrera.demo.product.infrastructure.service;

import static cl.gbarrera.demo.util.Messages.PRODUCT_NOT_FOUND;

import cl.gbarrera.demo.product.application.dto.ProductDTO;
import cl.gbarrera.demo.product.application.dto.ProductRequestDTO;
import cl.gbarrera.demo.product.application.dto.ProductSearchCriteria;
import cl.gbarrera.demo.product.application.exception.InvalidProductException;
import cl.gbarrera.demo.product.application.service.ProductService;
import cl.gbarrera.demo.product.domain.Product;
import cl.gbarrera.demo.product.infrastructure.persistence.ProductRepository;
import cl.gbarrera.demo.product.infrastructure.persistence.specification.ProductSpecification;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;

  @Override
  @Cacheable(value = "product", key = "#id")
  public ProductDTO getProductById(Long id) {
    return productRepository
        .findById(id)
        .map(p -> new ProductDTO(p.getId(), p.getName(), p.getPrice()))
        .orElseThrow(
            () ->
                new InvalidProductException(
                    String.format(PRODUCT_NOT_FOUND), HttpStatus.NOT_FOUND));
  }

  @Override
  @Cacheable(value = "products")
  public List<ProductDTO> getAllProducts() {
    return productRepository.findAll().stream()
        .map(p -> new ProductDTO(p.getId(), p.getName(), p.getPrice()))
        .collect(Collectors.toList());
  }

  @Override
  @CacheEvict(value = "products", allEntries = true)
  public ProductDTO createProduct(ProductRequestDTO productRequestDTO) {
    Product product = new Product();
    product.setName(productRequestDTO.getName());
    product.setPrice(productRequestDTO.getPrice());
    Product savedProduct = productRepository.save(product);
    return new ProductDTO(savedProduct.getId(), savedProduct.getName(), savedProduct.getPrice());
  }

  @Caching(
      put = {@CachePut(value = "product", key = "#id")},
      evict = {@CacheEvict(value = "products", allEntries = true)})
  @Override
  @CacheEvict(value = "products", allEntries = true)
  public ProductDTO updateProduct(Long id, ProductRequestDTO productRequestDTO) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(
                () -> new InvalidProductException(PRODUCT_NOT_FOUND, HttpStatus.BAD_REQUEST));

    product.setName(productRequestDTO.getName());
    product.setPrice(productRequestDTO.getPrice());

    Product updatedProduct = productRepository.save(product);
    return new ProductDTO(
        updatedProduct.getId(), updatedProduct.getName(), updatedProduct.getPrice());
  }

  @Caching(
      evict = {
        @CacheEvict(value = "product", key = "#id"),
        @CacheEvict(value = "products", allEntries = true)
      })
  @Override
  public void deleteProduct(Long id) {
    if (!productRepository.existsById(id)) {
      throw new InvalidProductException(PRODUCT_NOT_FOUND, HttpStatus.BAD_REQUEST);
    }
    productRepository.deleteById(id);
  }

  @Override
  public Page<Product> search(ProductSearchCriteria criteria) {
    Pageable pageable =
        PageRequest.of(
            criteria.getPage(),
            criteria.getSize(),
            Sort.by(Sort.Direction.fromString(criteria.getSortDir()), criteria.getSortBy()));

    Specification<Product> spec = ProductSpecification.byCriteria(criteria);

    return productRepository.findAll(spec, pageable);
  }
}
