package cl.gbarrera.demo.service;

import org.springframework.stereotype.Service;
import cl.gbarrera.demo.model.Product;  // Asegúrate de importar el modelo Product

import java.util.Arrays;
import java.util.List;

@Service
public class ProductService {
    private final List<Product> products = Arrays.asList(
            new Product(1, "Product 1", 100000),
            new Product(2, "Product 2", 500000)
    );

    public List<Product> getAllProducts() {
        return products;
    }
}
