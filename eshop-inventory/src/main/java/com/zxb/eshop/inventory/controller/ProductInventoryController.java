package com.zxb.eshop.inventory.controller;

import com.zxb.eshop.inventory.model.ProductInventory;
import com.zxb.eshop.inventory.request.ProductInventoryCacheRefreshRequest;
import com.zxb.eshop.inventory.request.ProductInventoryDBUpdateRequest;
import com.zxb.eshop.inventory.request.Request;
import com.zxb.eshop.inventory.service.ProductInventoryService;
import com.zxb.eshop.inventory.service.RequestAsyncProcessService;
import com.zxb.eshop.inventory.vo.Result;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by xuery on 2018/1/23.
 */
@Controller
public class ProductInventoryController {

    private static final Logger logger= LoggerFactory.getLogger(ProductInventoryController.class);

    @Autowired
    private RequestAsyncProcessService requestAsyncProcessService;

    @Autowired
    private ProductInventoryService productInventoryService;

    /**
     * 更新商品库存
     */
    @RequestMapping("/updateProductInventory")
    @ResponseBody
    public Result<Boolean> updateProductInventory(ProductInventory productInventory){
        logger.info(">>>>>updateProductInventory start:"+productInventory);
        Result<Boolean> result = new Result<>();
        try{
            Request request = new ProductInventoryDBUpdateRequest(productInventory, productInventoryService);
            requestAsyncProcessService.process(request);
            result.setSuccess(true);
            result.setObj(true);
        } catch (Exception e){
            logger.error(">>>updateProductInventory error",e);
            result.setObj(false);
        }
        return result;
    }

    @RequestMapping("/getProductInventory")
    @ResponseBody
    public Result<ProductInventory> getProductInventory(Integer productId){
        Result<ProductInventory> result = new Result<>(true);
        if(productId == null){
            result.setSuccess(false);
            result.setErrorMsg("请求参数productId不能为空");
            return result;
        }
        //正确的做法是先从缓存中查，如果缓存中没有，才需要进行下面的操作 add xuery 20170124
        //教程中没加这段代码是会有问题的，不加则相当于每次都得去异步从数据库中读取数据并刷到缓存，不合理
        ProductInventory productInventory = productInventoryService.getProductInventoryCache(productId);
        logger.info(">>>>>getProductInventory, redis缓存中数据："+productInventory);
        if(productInventory != null){
            result.setObj(productInventory);
            return result;
        }
        try{
            Request request = new ProductInventoryCacheRefreshRequest(productId, productInventoryService);
            requestAsyncProcessService.process(request);
            //将请求扔到service异步处理后，就需要while(true)一会，在这里hang住
            //去尝试等待前面有商品更新的操作，同时缓存刷新的操作，将最新的数据刷新到缓存中
            long startTime = System.currentTimeMillis();
            long endTime = 0L;
            long waitTime = 0L;

            while(true){

                //一般公司里面，面向用户的读请求控制在200ms就可以了
                // 模拟双线程，一个修改库存，一个读取库存，超时时间需要改变下
                if(waitTime > 10000+200){
                    break;
                }

                //尝试去redis缓存中读取，读取到了直接返回
                productInventory = productInventoryService.getProductInventoryCache(productId);

                if(productInventory != null){
                    result.setObj(productInventory);
                    return result;
                }
                //如果缓存中没有，则等待一段时间
                else {
                    Thread.sleep(20);
                    endTime = System.currentTimeMillis();
                    waitTime= endTime - startTime;
                }
            }

            //超时处理,直接从数据库中读取，这里做的超时处理需要根据实际系统来，超过200ms还没从缓存中读取到，则直接从数据库中读取并缓存
            productInventory = productInventoryService.getProductInventory(productId);
            if(productInventory != null){
                //将数据刷到缓存中
                // 这个过程，实际上是一个读操作的过程，但是没有放在队列中串行去处理，还是有数据不一致的问题
                request = new ProductInventoryCacheRefreshRequest(productId, productInventoryService);
                requestAsyncProcessService.process(request);

                // 代码会运行到这里，只有三种情况：
                // 1、就是说，上一次也是读请求，数据刷入了redis，但是redis LRU算法给清理掉了，标志位还是false
                // 所以此时下一个读请求是从缓存中拿不到数据的，再放一个读Request进队列，让数据去刷新一下
                // 2、可能在200ms内，就是读请求在队列中一直积压着，没有等待到它执行（在实际生产环境中，基本是比较坑了）
                // 所以就直接查一次库，然后给队列里塞进去一个刷新缓存的请求
                // 3、数据库里本身就没有，缓存穿透，穿透redis，请求到达mysql库
                result.setObj(productInventory);
                return result;
            }
        }catch (Exception e){
            logger.error(">>>getProductInventory error",e);
            result.setSuccess(false);
            result.setErrorMsg(">>>getProductInventory服务器出错");
            return result;
        }
        result.setObj(new ProductInventory(productId, -1));
        return result;
    }
}
