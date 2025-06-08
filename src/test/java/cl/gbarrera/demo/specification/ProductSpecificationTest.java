package cl.gbarrera.demo.specification;

import cl.gbarrera.demo.product.application.dto.ProductSearchCriteria;
import cl.gbarrera.demo.product.domain.Product;
import cl.gbarrera.demo.product.infrastructure.persistence.specification.ProductSpecification;
import jakarta.persistence.criteria.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.junit.jupiter.api.Test;

import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductSpecificationTest {


    @Test
    void byCriteria_shouldReturnNonNullSpecification_whenCriteriaIsEmpty() {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setName(null);
        criteria.setMinPrice(null);
        criteria.setMaxPrice(null);

        Specification<Product> spec = ProductSpecification.byCriteria(criteria);

        assertNotNull(spec);

        Root<Product> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder builder = mock(CriteriaBuilder.class);

        when(builder.and()).thenReturn(mock(Predicate.class));

        Predicate result = spec.toPredicate(root, query, builder);

        assertNotNull(result);

        verify(builder, never()).like(any(), anyString());
        verify(builder, never()).greaterThanOrEqualTo(any(), anyLong());
        verify(builder, never()).lessThanOrEqualTo(any(), anyLong());
    }

    @Test
    void byCriteria_shouldReturnNonNullSpecification_whenAllCriteriaAreSet() {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setName("test");
        criteria.setMinPrice(10L);
        criteria.setMaxPrice(100L);

        Specification<Product> spec = ProductSpecification.byCriteria(criteria);

        Root<Product> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder builder = mock(CriteriaBuilder.class);

        @SuppressWarnings("unchecked")
        Path<Object> pathName = mock(Path.class);

        when(root.get("name")).thenReturn(pathName);
        when(builder.lower((Expression<String>) (Object) pathName)).thenReturn(mock(Expression.class));
        when(builder.like(any(Expression.class), eq("%test%"))).thenReturn(mock(Predicate.class));

        when(root.get("price")).thenReturn(mock(Path.class));
        when(builder.greaterThanOrEqualTo(any(), eq(10L))).thenReturn(mock(Predicate.class));
        when(builder.lessThanOrEqualTo(any(), eq(100L))).thenReturn(mock(Predicate.class));
        when(builder.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        Predicate result = spec.toPredicate(root, query, builder);
        assertNotNull(result);
    }

    @Test
    void byCriteria_shouldCoverNameCondition_whenNameIsSet() {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setName("test");

        Specification<Product> spec = ProductSpecification.byCriteria(criteria);
        Root<Product> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder builder = mock(CriteriaBuilder.class);

        @SuppressWarnings("unchecked")
        Path<Object> pathName = mock(Path.class);
        when(root.get("name")).thenReturn(pathName);
        when(builder.lower((Expression<String>) (Object) pathName)).thenReturn(mock(Expression.class));
        when(builder.like(any(Expression.class), eq("%test%"))).thenReturn(mock(Predicate.class));
        when(root.get("price")).thenReturn(mock(Path.class));
        Predicate minPricePredicate = mock(Predicate.class);
        Predicate maxPricePredicate = mock(Predicate.class);
        when(builder.greaterThanOrEqualTo(
                (Expression<Long>) any(Expression.class),
                (Long) anyLong()
        )).thenReturn(minPricePredicate);

        when(builder.lessThanOrEqualTo(
                (Expression<Long>) any(Expression.class),
                (Long) anyLong()
        )).thenReturn(maxPricePredicate);
        when(builder.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        Predicate result = spec.toPredicate(root, query, builder);
        assertNotNull(result);
    }

    @Test
    void byCriteria_shouldCoverNameCondition_whenNameIsNullOrEmpty() {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setName("");

        Specification<Product> spec = ProductSpecification.byCriteria(criteria);
        Root<Product> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder builder = mock(CriteriaBuilder.class);

        when(root.get("price")).thenReturn(mock(Path.class));
        Predicate minPricePredicate = mock(Predicate.class);
        Predicate maxPricePredicate = mock(Predicate.class);
        when(builder.greaterThanOrEqualTo(
                (Expression<Long>) any(Expression.class),
                (Long) anyLong()
        )).thenReturn(minPricePredicate);

        when(builder.lessThanOrEqualTo(
                (Expression<Long>) any(Expression.class),
                (Long) anyLong()
        )).thenReturn(maxPricePredicate);
        when(builder.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        Predicate result = spec.toPredicate(root, query, builder);
        assertNotNull(result);
    }

    @Test
    void constructor_shouldBePrivate() throws Exception {
        Constructor<ProductSpecification> constructor = ProductSpecification.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

}
