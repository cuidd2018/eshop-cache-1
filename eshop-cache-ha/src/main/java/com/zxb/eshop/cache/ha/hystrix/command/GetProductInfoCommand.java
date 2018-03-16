package com.zxb.eshop.cache.ha.hystrix.command;

import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.*;
import com.zxb.eshop.cache.ha.cache.local.BrandCache;
import com.zxb.eshop.cache.ha.cache.local.LocationCache;
import com.zxb.eshop.cache.ha.http.MyHttpUtil;
import com.zxb.eshop.cache.ha.model.ProductInfo;
import com.zxb.eshop.cache.ha.vo.MyHttpResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xuery on 2018/2/28.
 */
public class GetProductInfoCommand extends HystrixCommand<ProductInfo> {

    private Long productId;

    public GetProductInfoCommand(Long productId) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("GetProductInfoGroup")) //指定command group
                .andCommandKey(HystrixCommandKey.Factory.asKey("GetProductInfoCommand")) //指定command key
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("GetProductInfoPool")) //指定command key自己的线程池，如果不指定，默认是comamnd group对应的pool
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                        .withCoreSize(15) //指定核心线程池的大小
                        .withMaxQueueSize(10) //等待队列的大小，如果不设置则默认为0
                        .withQueueSizeRejectionThreshold(8)) //等待队列中个数达到多少时，开始拒绝请求
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withCircuitBreakerEnabled(true)
                        .withCircuitBreakerRequestVolumeThreshold(30)
                        .withCircuitBreakerErrorThresholdPercentage(40)
                        .withCircuitBreakerSleepWindowInMilliseconds(3000) //三个参数的意思是：在3s中内必须要有至少30个请求，而且异常请求量要达到40%*30=12
//                        .withExecutionTimeoutInMilliseconds(20000) //默认1000ms, 这个值现在设置的比较大，为了防止模拟实验时全部执行超时，实际应用中需要根据场景设置
                        .withExecutionTimeoutInMilliseconds(3000)
                        .withFallbackIsolationSemaphoreMaxConcurrentRequests(30) //降级并发数
                )
        );
        this.productId = productId;
    }

    @Override
    protected ProductInfo run() throws Exception {
//        System.out.println(Thread.currentThread().getName()+" is running...");
        System.out.println("调用接口，查询商品数据，productId=" + productId);

        if(productId.equals(-1L)){
            throw new Exception();
        }

        if(productId.equals(-2L)){
//            Thread.sleep(3000); //hang住, 需要将线程池的超时时间设置长一点，否则这里会直接超时，也会走降级逻辑
            throw new Exception();
        }

        if(productId.equals(-3L)){
            Thread.sleep(3000); //肯定超时了
        }

        String url = "http://localhost:8082/getProductInfo";
        Map<String, String> map = new HashMap<>();
        map.put("productId", String.valueOf(productId));
        MyHttpResponse response = MyHttpUtil.doHttpGet(url, map);
        return JSONObject.parseObject(response.getResponseBody(), ProductInfo.class);
    }

    /**
     * 重写getCacheKey方法，开启request cache
     * 重写了该方法必须初始化HystrixRequestContext
     *
     * @return
     */
//    @Override
//    protected String getCacheKey() {
//        //指定key，每次会先通过这个key从request cache中拿，拿到则不再发出网络请求
//        return "product_info_" + productId;
//    }

    @Override
    protected ProductInfo getFallback() {
        //stubbed fallback
        /*ProductInfo productInfo = new ProductInfo();
        // 从请求参数中获取到的唯一条数据
        productInfo.setId(productId);
        // 从本地缓存中获取一些数据
        productInfo.setBrandId(BrandCache.getBrandId(productId));
        productInfo.setBrandName(BrandCache.getBrandName(productInfo.getBrandId()));
        productInfo.setCityId(LocationCache.getCityId(productId));
        // 手动填充一些默认的数据
        productInfo.setColor("默认颜色");
        productInfo.setModifiedTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        productInfo.setName("默认商品");
        productInfo.setPictureList("default.jpg");
        productInfo.setPrice(0.0);
        productInfo.setService("默认售后服务");
        productInfo.setShopId(-1L);
        productInfo.setSize("默认大小");
        productInfo.setSpecification("默认规格");*/
        ProductInfo productInfo = new FirstLevelFallbackCommand(productId).execute();
        return productInfo;
    }

    private static class FirstLevelFallbackCommand extends HystrixCommand<ProductInfo>{
        private Long productId;

        public FirstLevelFallbackCommand(Long productId) {
            // 第一级的降级策略，因为这个command是运行在fallback中的
            // 所以至关重要的一点是，在做多级降级的时候，要将降级command的线程池单独做一个出来
            // 如果主流程的command都失败了，可能线程池都已经被占满了
            // 降级command必须用自己的独立的线程池
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ProductInfoService"))
                    .andCommandKey(HystrixCommandKey.Factory.asKey("FirstLevelFallbackCommand"))
                    .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("FirstLevelFallbackPool"))
            );
            this.productId = productId;
        }

        @Override
        protected ProductInfo run() throws Exception {
            if(productId == -2L){
                throw new Exception();
            }
            ProductInfo productInfo = new ProductInfo();
            productInfo.setName("第一级降级商品");
            return productInfo;
        }

        @Override
        protected ProductInfo getFallback() {
            ProductInfo productInfo = new ProductInfo();
            productInfo.setName("第二级降级商品");
            return productInfo;
        }
    }
}
