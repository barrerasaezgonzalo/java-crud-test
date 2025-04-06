package cl.gbarrera.demo.service;

import cl.gbarrera.demo.repository.ProductRepository;
import org.springframework.stereotype.Service;
import cl.gbarrera.demo.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
//import org.springframework.beans.factory.annotation.Autowired;
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Flux<Product> getAllProducts() {
        return Flux.fromIterable(productRepository.findAll());
    }

    public Mono<Product> getProductById(Long id){
        return Mono.justOrEmpty(productRepository.findById(id));
    }

    public Mono<Product> createProduct(Product product){
        return Mono.just(productRepository.save(product));
    }

    public Mono<Product> updateProduct(Long id, Product updatedProduct) {
        return Mono.justOrEmpty(productRepository.findById(id))
                .flatMap(existingProduct -> {
                    existingProduct.setName(updatedProduct.getName());
                    existingProduct.setPrice(updatedProduct.getPrice());
                    return Mono.just(productRepository.save(existingProduct));
                });
    }

    public Mono<Void> deleteProduct(Long id) {
        return Mono.fromCallable(() -> productRepository.findById(id))
                .flatMap(optionalProduct -> optionalProduct
                        .map(product -> Mono.fromRunnable(() -> productRepository.delete(product)))
                        .orElse(Mono.empty())).then();
    }
}
