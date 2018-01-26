package com.zxb.eshop.inventory.mapper;

import com.zxb.eshop.inventory.model.ProductInventory;
import org.apache.ibatis.annotations.Param;

/**
 * Created by xuery on 2018/1/23.
 */
public interface ProductInventoryMapper {

    /**
     * 更新数据库库存
     * @param productInventory
     */
    void updateProductInventory(ProductInventory productInventory);

    /**
     * 直接数据库查询库存
     * @param productId
     * @return
     */
    ProductInventory findProductInventory(@Param("productId") Integer productId);
}
