package cl.gbarrera.demo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class ProductService {
    private final String FILE_PATH = "src/main/resources/products.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Método para obtener todos los productos
    public List<Product> getAllProducts() {
        try {
            return objectMapper.readValue(new File(FILE_PATH), new TypeReference<List<Product>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo JSON", e);
        }
    }
}
