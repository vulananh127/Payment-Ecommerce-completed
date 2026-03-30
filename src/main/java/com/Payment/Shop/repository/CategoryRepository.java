package com.Payment.Shop.repository;

import com.Payment.Shop.dto.response.BaseCategoryResponse;
import com.Payment.Shop.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;


public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByCategoryName(String categoryName);

    Optional<Category> findByCategoryName(String categoryName);

    //   // Tính số sản phẩm theo danh mục
    // @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    // Long countProductsByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT c.id, COUNT(p) FROM Product p RIGHT JOIN p.category c GROUP BY c.id")
    List<Object[]> countProductsGroupByCategory();
}
