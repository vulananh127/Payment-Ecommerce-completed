package com.Payment.Shop.repository;

import com.Payment.Shop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
//     @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category " +
//            "WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
//            "AND (:categoryId IS NULL OR p.category.id = :categoryId)")
//     List<Product> findAllWithFilters(
//             @Param("name") String name,
//             @Param("categoryId") Long categoryId);

       @Query("""
       SELECT p FROM Product p
       LEFT JOIN FETCH p.category
       WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CAST(:name AS text)))
       AND (:categoryId IS NULL OR p.category.id = :categoryId)
       """)
       List<Product> findAllWithFilters(
              @Param("name") String name,
              @Param("categoryId") Long categoryId);

       // @Query(value = """
       // SELECT p FROM products p
       // LEFT JOIN category c ON c.id = p.category_id
       // WHERE (:name IS NULL OR p.name ILIKE CONCAT('%', :name, '%'))
       // AND (:categoryId IS NULL OR p.category_id = :categoryId)
       // """, nativeQuery = true)
       // List<Product> findAllWithFilters(String name, Long categoryId);

}
