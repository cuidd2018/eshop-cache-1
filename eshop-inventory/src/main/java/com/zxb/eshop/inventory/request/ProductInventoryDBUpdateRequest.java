package com.zxb.eshop.inventory.request;

import com.zxb.eshop.inventory.model.ProductInventory;
import com.zxb.eshop.inventory.service.ProductInventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 先删除redis缓存，再更新数据库请求类
 * Created by xuery on 2018/1/23.
 */
public class ProductInventoryDBUpdateRequest implements Request{

    private static final Logger logger = LoggerFactory.getLogger(ProductInventoryDBUpdateRequest.class);

    private ProductInventory productInventory;

    private ProductInventoryService productInventoryService;

    public ProductInventoryDBUpdateRequest(ProductInventory productInventory, ProductInventoryService productInventoryService) {
        this.productInventory = productInventory;
        this.productInventoryService = productInventoryService;
    }

    @Override
    public void process() {
        logger.info(">>>>>ProductInventoryDBUpdateRequest-process,删除缓存开始,"+productInventory);
        //先删除redis缓存
        productInventoryService.removeProductInventoryCache(productInventory);

        //模拟等待10s，在这10s之内如果有读库存的操作，最后能否保证缓存数据库一致
        try{
            Thread.sleep(10000);
        } catch (Exception e){
            logger.info("ProductInventoryDBUpdateRequest-process error");
        }
        logger.info(">>>>>ProductInventoryDBUpdateRequest-process,修改数据库开始,"+productInventory);
        //再修改数据库库存
        productInventoryService.updateProductInventory(productInventory);
    }

    @Override
    public Integer getProductId() {
        return productInventory.getProductId();
    }

}
