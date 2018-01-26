package com.zxb.eshop.inventory.service.impl;

import com.zxb.eshop.inventory.dao.RedisDAO;
import com.zxb.eshop.inventory.mapper.ProductInventoryMapper;
import com.zxb.eshop.inventory.model.ProductInventory;
import com.zxb.eshop.inventory.service.ProductInventoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xuery on 2018/1/23.
 */
@Service
public class ProductInventoryServiceImpl implements ProductInventoryService {

    @Autowired
    private ProductInventoryMapper productInventoryMapper;

    @Autowired
    private RedisDAO redisDAO;

    private static final String PRODUCT_INVENTORY = "product_inventory_";

    @Override
    public void updateProductInventory(ProductInventory productInventory) {
        productInventoryMapper.updateProductInventory(productInventory);
    }

    @Override
    public void removeProductInventoryCache(ProductInventory productInventory) {
        String key = PRODUCT_INVENTORY + productInventory.getProductId();
        redisDAO.delete(key);
    }

    @Override
    public ProductInventory getProductInventory(Integer productId) {
        return productInventoryMapper.findProductInventory(productId);
    }

    @Override
    public ProductInventory getProductInventoryCache(Integer productId) {
        String key = PRODUCT_INVENTORY + productId;
        String inventoryCnt = redisDAO.get(key);
        if(StringUtils.isNotBlank(inventoryCnt)){
            return new ProductInventory(productId, Integer.parseInt(inventoryCnt));
        } else {
            return null;
        }
    }

    @Override
    public void setProductInventoryCache(ProductInventory productInventory) {
        String key = PRODUCT_INVENTORY + productInventory.getProductId();
        redisDAO.set(key, String.valueOf(productInventory.getInventoryCnt()));
    }
}
