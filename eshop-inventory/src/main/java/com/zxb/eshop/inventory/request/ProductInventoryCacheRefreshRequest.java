package com.zxb.eshop.inventory.request;

import com.zxb.eshop.inventory.model.ProductInventory;
import com.zxb.eshop.inventory.service.ProductInventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xuery on 2018/1/23.
 */
public class ProductInventoryCacheRefreshRequest implements Request {

    private static final Logger logger = LoggerFactory.getLogger(ProductInventoryCacheRefreshRequest.class);

    private Integer productId;

    private ProductInventoryService productInventoryService;

    public ProductInventoryCacheRefreshRequest(Integer productId, ProductInventoryService productInventoryService) {
        this.productId = productId;
        this.productInventoryService = productInventoryService;
    }

    @Override
    public void process() {
        logger.info(">>>>>ProductInventoryCacheRefreshRequest-process, start");
        //先查询
        ProductInventory newProInventory = productInventoryService.getProductInventory(productId);
        logger.info(">>>>>ProductInventoryCacheRefreshRequest-process:"+newProInventory);
        if(newProInventory == null){
            logger.info(">>>>>ProductInventoryCacheRefreshRequest-process,从数据库中查出的库存信息为空，productId="+productId);
            return;
        }
        //更新缓存
        productInventoryService.setProductInventoryCache(newProInventory);
    }

    @Override
    public Integer getProductId() {
        return productId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductInventoryCacheRefreshRequest that = (ProductInventoryCacheRefreshRequest) o;

        return productId.equals(that.productId);
    }

    @Override
    public int hashCode() {
        return productId.hashCode();
    }
}
