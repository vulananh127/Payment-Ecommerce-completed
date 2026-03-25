package com.Payment.Shop.repository;

import com.Payment.Shop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
        SELECT DISTINCT p FROM Product p
        LEFT JOIN FETCH p.category
        LEFT JOIN FETCH p.productVariants v
        LEFT JOIN FETCH v.productVariantOptions
        WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CAST(:name AS text)))
        AND (:categoryId IS NULL OR p.category.id = :categoryId)
        ORDER BY p.id DESC
    """)
    List<Product> findAllWithFilters(
            @Param("name") String name,
            @Param("categoryId") Long categoryId
    );


    @Query("""
        SELECT DISTINCT p FROM Product p
        LEFT JOIN FETCH p.category
        LEFT JOIN FETCH p.productVariants v
        LEFT JOIN FETCH v.productVariantOptions
        ORDER BY p.id DESC
    """)
    List<Product> findAllWithVariants();

}