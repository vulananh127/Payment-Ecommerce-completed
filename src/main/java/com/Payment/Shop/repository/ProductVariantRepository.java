package com.Payment.Shop.repository;

import com.Payment.Shop.entity.ProductVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

        // Query mới — dùng khi tạo order, cần lấy thêm product name
    @Query("SELECT pv FROM ProductVariant pv JOIN FETCH pv.product WHERE pv.id IN :variantIds")
    List<ProductVariant> findAllByIdWithProduct(@Param("variantIds") List<Long> variantIds);

    // kiểm tra variant có đang được dùng trong order_item không
    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi WHERE oi.productVariant.id = :variantId")
    boolean existsInOrderItem(@Param("variantId") Long variantId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.id IN :variantIds")
    List<ProductVariant> findAllByIdWithLockIn(@Param("variantIds") List<Long> variantIds);

    @Modifying
    @Query("UPDATE ProductVariant pv SET pv.stock = pv.stock - :requestQuantity " +
            "WHERE pv.id = :id AND pv.stock >= :requestQuantity")
    int updateStockConditionally(@Param("id") Long id,
                                 @Param("requestQuantity") Integer requestQuantity);

        @Modifying
    @Query("""
        UPDATE ProductVariant v
        SET v.stock = v.stock + :qty
        WHERE v.id = :variantId
    """)
    int increaseStock(@Param("variantId") Long variantId,
                    @Param("qty") Integer qty);

}
