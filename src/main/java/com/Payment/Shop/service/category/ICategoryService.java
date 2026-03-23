// ===== ICategoryService.java =====
package com.Payment.Shop.service.category;
 
import com.Payment.Shop.dto.request.CreateCategoryRequest;
import com.Payment.Shop.dto.request.UpdateCategoryRequest;
import com.Payment.Shop.dto.response.BaseCategoryResponse;
 
import java.util.List;
 
public interface ICategoryService {
 
    List<BaseCategoryResponse> getAllCategories();
 
    BaseCategoryResponse getCategoryById(Long id);
 
    BaseCategoryResponse createCategory(CreateCategoryRequest request);
 
    BaseCategoryResponse updateCategory(Long id, UpdateCategoryRequest request);
 
    void deleteCategory(Long id);
}