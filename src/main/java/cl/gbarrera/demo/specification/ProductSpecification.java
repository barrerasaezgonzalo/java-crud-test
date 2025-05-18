package cl.gbarrera.demo.specification;

import cl.gbarrera.demo.dto.ProductSearchCriteria;
import cl.gbarrera.demo.model.Product;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class ProductSpecification {
    public static Specification<Product> byCriteria(ProductSearchCriteria criteria) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getName() != null && !criteria.getName().isEmpty()) {
                predicates.add(builder.like(builder.lower(root.get("name")), "%" + criteria.getName().toLowerCase() + "%"));
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
