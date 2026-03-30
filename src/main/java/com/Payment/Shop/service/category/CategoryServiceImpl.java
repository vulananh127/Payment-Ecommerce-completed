// ===== CategoryServiceImpl.java =====
package com.Payment.Shop.service.category;
 
import com.Payment.Shop.dto.request.CreateCategoryRequest;
import com.Payment.Shop.dto.request.UpdateCategoryRequest;
import com.Payment.Shop.dto.response.BaseCategoryResponse;
import com.Payment.Shop.entity.Category;
import com.Payment.Shop.exception.DuplicateException;
import com.Payment.Shop.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
 
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements ICategoryService {
 
    // Inject ModelMapper vào CategoryServiceImpl
    private final ModelMapper modelMapper;
    private final CategoryRepository categoryRepository;
 
    // @Override
    // public List<BaseCategoryResponse> getAllCategories() {
    //     return categoryRepository.findAll()
    //             .stream()
    //             .map(this::toResponse)
    //             .collect(Collectors.toList());
    // }
    @Override
public List<BaseCategoryResponse> getAllCategories() {
    // Lấy map: categoryId -> productCount
    Map<Long, Long> countMap = categoryRepository.countProductsGroupByCategory()
            .stream()
            .collect(Collectors.toMap(
                    row -> (Long) row[0],
                    row -> (Long) row[1]
            ));

    return categoryRepository.findAll()
            .stream()
            .map(c -> {
                BaseCategoryResponse res = modelMapper.map(c, BaseCategoryResponse.class);
                res.setProductCount(countMap.getOrDefault(c.getId(), 0L));
                return res;
            })
            .collect(Collectors.toList());
}
 
    @Override
    public BaseCategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
        return toResponse(category);
    }
 
    @Override
    @Transactional
    public BaseCategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByCategoryName(request.getCategoryName())) {
            throw new RuntimeException("Category name already exists");
        }
        Category category = new Category();
        category.setCategoryName(request.getCategoryName());
        Category saved = categoryRepository.save(category);
        return toResponse(saved);
    }
 
    @Override
    @Transactional
    public BaseCategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
 
        // Kiểm tra tên mới có trùng không (trừ chính nó)
        if (!category.getCategoryName().equals(request.getCategoryName())
                && categoryRepository.existsByCategoryName(request.getCategoryName())) {
            throw new RuntimeException("Category name already exists");
        }
 
        category.setCategoryName(request.getCategoryName());
        Category saved = categoryRepository.save(category);
        return toResponse(saved);
    }
 
    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found: " + id);
        }
        categoryRepository.deleteById(id);
    }
 

    private BaseCategoryResponse toResponse(Category category) {
    return modelMapper.map(category, BaseCategoryResponse.class);
}
}
