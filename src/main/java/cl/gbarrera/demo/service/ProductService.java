package cl.gbarrera.demo.service;

import org.springframework.stereotype.Service;
import cl.gbarrera.demo.model.Product;  // Asegúrate de importar el modelo Product
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final List<Product> products = new ArrayList<>(Arrays.asList(
            new Product(1L, "Product 1", 100000),
            new Product(2L, "Product 2", 500000)
    ));

    public Flux<Product> getAllProducts() {
        return Flux.fromIterable(products);
    }

    public Mono<Product> getProductById(Long id){
        return Flux.fromIterable(products)
                .filter(product -> product.getId() == id)
                .next();
    }

    public Mono<Product> createProduct(Product product){
        products.add(product);
        return Mono.just(product);
    }

    public Mono<Product> updateProduct(Long id, Product product) {
        Optional<Product> existingProduct = products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();

        if (existingProduct.isPresent()) {
            Product updateProduct = existingProduct.get();
            updateProduct.setName(product.getName());
            updateProduct.setPrice(product.getPrice());

            return Mono.just(updateProduct);
        } else {
            return Mono.empty();
        }
    }

    public Mono<Void> deleteProduct(Long id) {
        boolean removed = products.removeIf(p -> p.getId().equals(id));

        if (removed) {
            return Mono.empty();
        } else {
            return null;
        }
    }



}
