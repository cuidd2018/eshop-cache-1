package com.zxb.eshop.cache.ha.hystrix.command;

import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.*;
import com.zxb.eshop.cache.ha.http.MyHttpUtil;
import com.zxb.eshop.cache.ha.model.ProductInfo;
import com.zxb.eshop.cache.ha.vo.MyHttpResponse;

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
            Thread.sleep(3000); //hang住, 需要将线程池的超时时间设置长一点，否则这里会直接超时，也会走降级逻辑
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
        ProductInfo productInfo = new ProductInfo();
        productInfo.setName("降级商品");
        return productInfo;
    }
}
