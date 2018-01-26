package com.zxb.eshop.inventory.service;

import com.zxb.eshop.inventory.model.ProductInventory;

/**
 *商品库存service类
 *  Created by xuery on 2018/1/23.
 */
public interface ProductInventoryService {

    /**
     * 更新库存db
     * @param productInventory
     */
    void updateProductInventory(ProductInventory productInventory);

    /**
     * 删除库存缓存
     * @param productInventory
     */
    void removeProductInventoryCache(ProductInventory productInventory);

    /**
     * 获取库存信息
     * @param productId
     */
    ProductInventory getProductInventory(Integer productId);

    /**
     * 从缓存中获取数据
     * @param productId
     * @return
     */
    ProductInventory getProductInventoryCache(Integer productId);

    /**
     * 设置库存缓存
     * @param productInventory
     */
    void setProductInventoryCache(ProductInventory productInventory);
}
