package com.Payment.Shop.service.product;

import com.Payment.Shop.dto.request.CreateProductRequest;
import com.Payment.Shop.dto.request.CreateProductVariantRequest;
import com.Payment.Shop.dto.request.UpdateProductRequest;
import com.Payment.Shop.dto.response.BaseProductResponse;
import com.Payment.Shop.dto.response.CategoryResponse;
import com.Payment.Shop.dto.response.ProductVariantResponse;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Set;

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
    public List<BaseProductResponse> getAllProducts(String name, Long categoryId) {
    if (name != null && !name.trim().isEmpty()) {
        name = "%" + name.trim() + "%";
    } else {
        name = null;
    }

    List<Product> products = productRepository.findAllWithFilters(name, categoryId);

    products.forEach(p -> {
        System.out.println("Product id=" + p.getId() + ", name=" + p.getName());
    });

    return products.stream()
        .map(this::mapProduct)
        .toList();
}

    @Override
public BaseProductResponse getProductById(Long id) {

    Product product =
            productRepository.findById(id)
                    .orElseThrow();

    return mapProduct(product);
}

     @Override
    @Transactional
    public BaseProductResponse createProduct(CreateProductRequest request) {

    Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Category not found"));

    Product product = new Product();
    product.setName(request.getName());
    product.setDescription(request.getDescription());
    product.setImageUrl(request.getImageUrl());
    product.setCategory(category);

    product.setProductVariants(new HashSet<>());
    // ===== no variant =====
    if (request.getVariants() == null || request.getVariants().isEmpty()) {
        ProductVariant variant = new ProductVariant();
        variant.setSku("DEFAULT");
        variant.setPrice(request.getPrice());
        variant.setStock(request.getStockQuantity());
        variant.setProduct(product);
        variant.setProductVariantOptions(new HashSet<>());
        product.getProductVariants().add(variant);
    }
    // ===== has variant =====
    else {
        for (CreateProductVariantRequest v : request.getVariants()) {
            ProductVariant variant = new ProductVariant();
            variant.setSku(generateSku(v.getAttributes()));
            variant.setPrice(v.getPrice());
            variant.setStock(v.getStockQuantity());
            variant.setProduct(product);
            variant.setProductVariantOptions(new HashSet<>());
            if (v.getAttributes() != null) {
                for (var e : v.getAttributes().entrySet()) {
                    ProductVariantOption opt =
                            new ProductVariantOption(
                                    e.getKey(),
                                    e.getValue()
                            );
                    opt.setProductVariant(variant);
                    variant.getProductVariantOptions().add(opt);
                }
            }
            product.getProductVariants().add(variant);
        }
    }
    Product saved = productRepository.save(product);
    return mapProduct(saved);
}
    
    @Override
    @Transactional
public BaseProductResponse updateProduct(Long id, UpdateProductRequest request) {

    Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));

    product.setName(request.getName());
    product.setDescription(request.getDescription());
    product.setImageUrl(request.getImageUrl());

    if (request.getCategoryId() != null) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        product.setCategory(category);
    }

    if (request.getVariants() != null) {
    Map<String, ProductVariant> existingBySku = product.getProductVariants()
            .stream()
            .filter(ProductVariant::isActive)
            .collect(Collectors.toMap(ProductVariant::getSku, v -> v));

    Set<String> requestedSkus = request.getVariants().stream()
            .map(v -> generateSku(v.getAttributes()))
            .collect(Collectors.toSet());

    // ✅ Dùng Iterator để tránh ConcurrentModificationException
    Iterator<ProductVariant> iterator = product.getProductVariants().iterator();
    while (iterator.hasNext()) {
        ProductVariant existing = iterator.next();
        if (existing.isActive() && !requestedSkus.contains(existing.getSku())) {
            if (productVariantRepository.existsInOrderItem(existing.getId())) {
                existing.setActive(false); // đã bán → chỉ ẩn
            } else {
                iterator.remove(); // chưa bán → orphanRemoval xóa hẳn
            }
        }
    }

    for (CreateProductVariantRequest v : request.getVariants()) {
        String sku = generateSku(v.getAttributes());
        ProductVariant existing = existingBySku.get(sku);

        if (existing != null) {
            existing.setPrice(v.getPrice());
            existing.setStock(v.getStockQuantity());
        } else {
            ProductVariant newVariant = new ProductVariant();
            newVariant.setSku(sku);
            newVariant.setPrice(v.getPrice());
            newVariant.setStock(v.getStockQuantity());
            newVariant.setActive(true);
            newVariant.setProduct(product);
            newVariant.setProductVariantOptions(new HashSet<>());

            if (v.getAttributes() != null) {
                for (var e : v.getAttributes().entrySet()) {
                    ProductVariantOption opt = new ProductVariantOption(e.getKey(), e.getValue());
                    opt.setProductVariant(newVariant);
                    newVariant.getProductVariantOptions().add(opt);
                }
            }
            product.getProductVariants().add(newVariant);
        }
    }
}
    

    Product saved = productRepository.save(product);
    return mapProduct(saved);
}

    private String generateSku(Map<String, String> attrs) {
    if (attrs == null || attrs.isEmpty()) {
        return "DEFAULT";
    }
    return attrs.values()
            .stream()
            .map(v -> v.toUpperCase().replace(" ", ""))
            .collect(Collectors.joining("-"));
}

    private BaseProductResponse mapProduct(Product product) {
        BaseProductResponse res = new BaseProductResponse();
        res.setId(product.getId());
        res.setName(product.getName());
        res.setDescription(product.getDescription());
        res.setImageUrl(product.getImageUrl());
        // ===== category =====
        if (product.getCategory() != null) {
            CategoryResponse c = new CategoryResponse();
            c.setId(product.getCategory().getId());
            c.setCategoryName(
                    product.getCategory().getCategoryName()
            );
            res.setCategory(c);
        }
        // ===== variants =====
        if (product.getProductVariants() != null) {
        List<ProductVariantResponse> list = product.getProductVariants()
                .stream()
                .filter(ProductVariant::isActive) // ← CHỈ trả active variant
                .map(v -> {
                    ProductVariantResponse r = new ProductVariantResponse();
                    r.setId(v.getId());
                    r.setSku(v.getSku());
                    r.setPrice(v.getPrice());
                    r.setStockQuantity(v.getStock());
                    Map<String, String> attrs = v.getProductVariantOptions().stream()
                            .collect(Collectors.toMap(
                                    ProductVariantOption::getAttribute,
                                    ProductVariantOption::getValue));
                    r.setAttributes(attrs);
                    return r;
                })
                .toList();
        res.setVariants(list);
        // price + stock lấy từ variant active đầu tiên
        list.stream().findFirst().ifPresent(v -> {
            res.setPrice(v.getPrice());
            res.setStockQuantity(v.getStockQuantity());
        });
        }

        return res;
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found: " + id);
        }
        boolean hasOrder = productRepository.existsByProductId(id);

    if (hasOrder) {
        throw new RuntimeException("Sản phẩm tồn tại trong lịch sử đơn hàng, không thể xóa");
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
public List<ProductVariant> findAllProductVariantByVariantIdWithProduct(List<Long> variantIds) {
    return productVariantRepository.findAllByIdWithProduct(variantIds);
}

    @Override
    public int updateStockOptimistic(Long variantId, Integer requestQuantity) {
        return productVariantRepository.updateStockConditionally(variantId, requestQuantity);
    }

    @Override
    @Transactional
    public void increaseStock(Long variantId, Integer qty) {

        int updated = productVariantRepository.increaseStock(variantId, qty);

        if (updated == 0) {
            throw new RuntimeException("Variant not found: " + variantId);
        }

        log.info("Increase stock variantId={}, +{}", variantId, qty);
    }
}