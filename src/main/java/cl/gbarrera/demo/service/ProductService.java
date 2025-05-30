package cl.gbarrera.demo.service;

import cl.gbarrera.demo.dto.ProductDTO;
import cl.gbarrera.demo.dto.ProductRequestDTO;
import cl.gbarrera.demo.dto.ProductSearchCriteria;
import cl.gbarrera.demo.model.Product;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {

    List<ProductDTO> getAllProducts();

    ProductDTO getProductById(Long id);

    ProductDTO createProduct(ProductRequestDTO productRequestDTO);

    ProductDTO updateProduct(Long id, ProductRequestDTO productRequestDTO);

    void deleteProduct(Long id);

    Page<Product> search(ProductSearchCriteria criteria);
    ;
}
