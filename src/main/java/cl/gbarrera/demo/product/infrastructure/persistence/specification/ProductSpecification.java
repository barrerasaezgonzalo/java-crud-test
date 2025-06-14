package cl.gbarrera.demo.product.infrastructure.persistence.specification;

import cl.gbarrera.demo.product.application.dto.ProductSearchCriteria;
import cl.gbarrera.demo.product.domain.Product;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

  private ProductSpecification() {}

  public static Specification<Product> byCriteria(ProductSearchCriteria criteria) {
    return (root, query, builder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (criteria.getName() != null && !criteria.getName().isEmpty()) {
        predicates.add(
            builder.like(
                builder.lower(root.get("name")), "%" + criteria.getName().toLowerCase() + "%"));
      }

      if (criteria.getMinPrice() != null) {
        predicates.add(builder.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice()));
      }

      if (criteria.getMaxPrice() != null) {
        predicates.add(builder.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice()));
      }

      return builder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
