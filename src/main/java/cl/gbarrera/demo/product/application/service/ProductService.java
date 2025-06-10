package cl.gbarrera.demo.product.application.service;

import cl.gbarrera.demo.product.application.dto.ProductDTO;
import cl.gbarrera.demo.product.application.dto.ProductRequestDTO;
import cl.gbarrera.demo.product.application.dto.ProductSearchCriteria;
import cl.gbarrera.demo.product.domain.Product;
import java.util.List;
import org.springframework.data.domain.Page;

public interface ProductService {
  ProductDTO getProductById(Long id);

  List<ProductDTO> getAllProducts();

  ProductDTO createProduct(ProductRequestDTO productRequestDTO);

  ProductDTO updateProduct(Long id, ProductRequestDTO productRequestDTO);

  void deleteProduct(Long id);

  Page<Product> search(ProductSearchCriteria criteria);
}
