package cl.gbarrera.demo.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long id) {
        super("Product with ID " + id + " not found");
    }
    public ProductNotFoundException(String name) {
        super("No products were found with the name: " + name);
    }
}