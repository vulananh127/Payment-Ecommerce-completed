package com.Payment.Shop.service.product;

import com.Payment.Shop.dto.request.CreateProductRequest;
import com.Payment.Shop.dto.request.UpdateProductRequest;
import com.Payment.Shop.dto.response.BaseProductResponse;
import com.Payment.Shop.entity.Category;
import com.Payment.Shop.entity.Product;
import com.Payment.Shop.entity.ProductVariant;
import com.Payment.Shop.entity.ProductVariantOption;
import com.Payment.Shop.repository.CategoryRepository;
import com.Payment.Shop.repository.ProductRepository;
import com.Payment.Shop.repository.ProductVariantRepository;
import com.Payment.Shop.repository.VariantOptionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final ProductVariantRepository productVariantRepository;
    private final VariantOptionRepository variantOptionRepository;
    private final CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public ProductServiceImpl(ProductRepository productRepository, ModelMapper modelMapper,
                               ProductVariantRepository productVariantRepository,
                               VariantOptionRepository variantOptionRepository,
                               CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
        this.productVariantRepository = productVariantRepository;
        this.variantOptionRepository = variantOptionRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public BaseProductResponse createProduct(CreateProductRequest request) {
        // Fetch category từ DB
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found: " + request.getCategoryId()));

        // Build Product
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        product.setDiscountPercent(request.getDiscountPercent());
        product.setCategory(category);
        product.setImageUrl(request.getImageUrl());
        product.setProductVariants(new ArrayList<>());

        entityManager.persist(product);

        // Build variants nếu có
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            for (var variantReq : request.getVariants()) {
                ProductVariant variant = new ProductVariant();
                variant.setSku(variantReq.getSku());
                variant.setStock(variantReq.getStockQuantity());
                variant.setPrice(variantReq.getPrice());
                variant.setProduct(product);
                variant.setProductVariantOptions(new ArrayList<>());

                if (variantReq.getAttributes() != null) {
                    for (var entry : variantReq.getAttributes().entrySet()) {
                        ProductVariantOption option = new ProductVariantOption(entry.getKey(), entry.getValue());
                        option.setProductVariant(variant);
                        variant.getProductVariantOptions().add(option);
                    }
                }

                entityManager.persist(variant);
                variant.getProductVariantOptions().forEach(entityManager::persist);
                product.getProductVariants().add(variant);
            }
        }

        entityManager.flush();
        entityManager.clear();

        return modelMapper.map(product, BaseProductResponse.class);
    }

    @Override
    // public List<BaseProductResponse> getAllProducts(String name, Long categoryId) {
    //     List<Product> products = productRepository.findAllWithFilters(name, categoryId);
    //     return products.stream()
    //             .map(p -> modelMapper.map(p, BaseProductResponse.class))
    //             .collect(Collectors.toList());
    // }

    public List<BaseProductResponse> getAllProducts(String name, Long categoryId) {
    if (name != null && !name.trim().isEmpty()) {
        name = "%" + name.trim() + "%";
    } else {
        name = null;
    }

    System.out.println("==== getAllProducts called ====");
System.out.println("name parameter: " + name + " (class: " + (name != null ? name.getClass() : "null") + ")");
System.out.println("categoryId parameter: " + categoryId + " (class: " + (categoryId != null ? categoryId.getClass() : "null") + ")");

    List<Product> products = productRepository.findAllWithFilters(name, categoryId);

    System.out.println("Number of products fetched: " + products.size());
    products.forEach(p -> {
        System.out.println("Product id=" + p.getId() + ", name=" + p.getName());
    });

    return products.stream()
            .map(p -> modelMapper.map(p, BaseProductResponse.class))
            .collect(Collectors.toList());
}


    @Override
    public BaseProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        return modelMapper.map(product, BaseProductResponse.class);
    }

    @Override
    @Transactional
    public BaseProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        product.setDiscountPercent(request.getDiscountPercent() != null ? request.getDiscountPercent() : 0.0);
        product.setImageUrl(request.getImageUrl());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found: " + request.getCategoryId()));
            product.setCategory(category);
        }

        // Cập nhật variants nếu có
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            // Xóa variants cũ
            product.getProductVariants().clear();
            productRepository.save(product); // flush delete trước

            // Thêm variants mới
            for (var variantReq : request.getVariants()) {
                ProductVariant variant = new ProductVariant();
                variant.setSku(variantReq.getSku());
                variant.setStock(variantReq.getStockQuantity());
                variant.setPrice(variantReq.getPrice());
                variant.setProduct(product);
                variant.setProductVariantOptions(new ArrayList<>());

                if (variantReq.getAttributes() != null) {
                    for (var entry : variantReq.getAttributes().entrySet()) {
                        ProductVariantOption option = new ProductVariantOption(entry.getKey(), entry.getValue());
                        option.setProductVariant(variant);
                        variant.getProductVariantOptions().add(option);
                    }
                }
                product.getProductVariants().add(variant);
            }
        }

        Product saved = productRepository.save(product);
        return modelMapper.map(saved, BaseProductResponse.class);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    public List<ProductVariant> findAllProductVariantByVariantId(List<Long> variantIds) {
        return productVariantRepository.findAllById(variantIds);
    }

    @Override
    public List<ProductVariant> findAllProductVariantByVariantIdWithLock(List<Long> variantIds) {
        return productVariantRepository.findAllByIdWithLockIn(variantIds);
    }

    @Override
    public void saveAllProductVariant(List<ProductVariant> productVariants) {
        try {
            productVariantRepository.saveAll(productVariants);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public int updateStockOptimistic(Long variantId, Integer requestQuantity) {
        return productVariantRepository.updateStockConditionally(variantId, requestQuantity);
    }
}