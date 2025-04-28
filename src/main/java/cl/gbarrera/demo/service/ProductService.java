package cl.gbarrera.demo.service;

import cl.gbarrera.demo.dto.ProductDTO;
import cl.gbarrera.demo.exception.ProductNotFoundException;
import cl.gbarrera.demo.repository.ProductRepository;
import org.springframework.stereotype.Service;
import cl.gbarrera.demo.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

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
                .map(product -> new ProductDTO(product.getId(), product.getName(), product.getPrice(),product.getCostPrice()));
    }

    public Mono<ProductDTO> getProductById(Long id){
        return Mono.justOrEmpty(productRepository.findById(id))
                .map(product -> new ProductDTO(
                        product.getId(),
                        product.getName(),
                        product.getPrice(),
                        product.getCostPrice()
                )).switchIfEmpty(Mono.error(new ProductNotFoundException(id)));
    };


    public Mono<ProductDTO> createProduct(ProductDTO productDTO){

        if (productDTO.getName() == null || productDTO.getName().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Product name cannot be null or empty"));
        }
        if (productDTO.getPrice() == null || productDTO.getPrice()<=0) {
            return Mono.error(new IllegalArgumentException("Price name cannot be null or empty"));
        }

        Product product = new Product(
                productDTO.getId(),
                productDTO.getName(),
                productDTO.getPrice(),
                productDTO.getCostPrice()
        );
        return Mono.fromCallable(() -> productRepository.save(product))
                .map(savedProduct -> new ProductDTO(
                        savedProduct.getId(),
                        savedProduct.getName(),
                        savedProduct.getPrice(),
                        savedProduct.getCostPrice()
                ));
    }

   public Mono<ProductDTO> updateProduct(Long id, ProductDTO productDTO) {

       if (productDTO.getName() == null || productDTO.getName().isEmpty()) {
           return Mono.error(new IllegalArgumentException("Product name cannot be null or empty"));
       }
       if (productDTO.getPrice() == null || productDTO.getPrice()<=0) {
           return Mono.error(new IllegalArgumentException("Price name cannot be null or empty"));
       }

        return Mono.justOrEmpty(productRepository.findById(id))
                .flatMap(existingProduct -> {
                    existingProduct.setName(productDTO.getName());
                    existingProduct.setPrice(productDTO.getPrice());
                    existingProduct.setCostPrice(productDTO.getCostPrice());
                    return Mono.just(productRepository.save(existingProduct))
                            .map(savedProduct -> new ProductDTO(
                                    savedProduct.getId(),
                                    savedProduct.getName(),
                                    savedProduct.getPrice(),
                                    savedProduct.getCostPrice()
                            ));
                })
               .switchIfEmpty(Mono.error(new ProductNotFoundException(id)));
    }


    public Mono<Void> deleteProduct(Long id){
        return Mono.justOrEmpty(productRepository.findById(id))
                 .switchIfEmpty(Mono.error(new ProductNotFoundException(id)))
                .flatMap(product -> {
                    productRepository.delete(product);
                    return Mono.empty();
                });
    }


    public Flux<ProductDTO> searchProductsByName(String name) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        if (products.isEmpty()) {
            return Flux.empty();
        }
        return Flux.fromIterable(products)
                .map(product -> new ProductDTO(
                        product.getId(),
                        product.getName(),
                        product.getPrice(),
                        product.getCostPrice()
                ));
    }
}
