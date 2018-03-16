package com.zxb.eshop.cache.ha.hystrix.command;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.netflix.hystrix.*;
import com.zxb.eshop.cache.ha.http.MyHttpUtil;
import com.zxb.eshop.cache.ha.model.ProductInfo;
import com.zxb.eshop.cache.ha.vo.MyHttpResponse;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xuery on 2018/3/15.
 */
public class GetProductInfosCollapser extends HystrixCollapser<List<ProductInfo>, ProductInfo, Long> {

    private Long productId;

    public GetProductInfosCollapser(Long productId) {
        super(Setter.withCollapserKey(HystrixCollapserKey.Factory.asKey("GetProductInfoCollapser"))
                .andCollapserPropertiesDefaults(HystrixCollapserProperties.Setter()
                        .withMaxRequestsInBatch(100)
                        .withTimerDelayInMilliseconds(20) //20ms触发一次collapser
                )
        );
        this.productId = productId;
    }

    @Override
    public Long getRequestArgument() {
        return productId;
    }

    @Override
    protected HystrixCommand<List<ProductInfo>> createCommand(Collection<CollapsedRequest<ProductInfo, Long>> requests) {
        //做记录
        StringBuilder sb = new StringBuilder();
        for (CollapsedRequest<ProductInfo, Long> request : requests) {
            sb.append(request.getArgument()).append(",");
        }
        String params = sb.toString();
        params = params.substring(0, params.length() - 1);
        System.out.println("createCommand方法执行，params=" + params);

        return new BatchCommand(requests);
    }

    @Override
    protected void mapResponseToRequests(List<ProductInfo> productInfos, Collection<CollapsedRequest<ProductInfo, Long>> requests) {
        int count = 0;
        for (CollapsedRequest<ProductInfo, Long> request : requests) {
            request.setResponse(productInfos.get(count++));
        }
    }

    @Override
    protected String getCacheKey() {
        return "product_info_" + productId;
    }

    private static final class BatchCommand extends HystrixCommand<List<ProductInfo>> {

        private Collection<CollapsedRequest<ProductInfo, Long>> requests;

        public BatchCommand(Collection<CollapsedRequest<ProductInfo, Long>> requests) {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ProductInfoService"))
                    .andCommandKey(HystrixCommandKey.Factory.asKey("GetProductInfosCollapserBatchCommand"))
                    .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(3000))
            );
            this.requests = requests;
        }

        @Override
        protected List<ProductInfo> run() throws Exception {
            //得到合并参数
            StringBuilder sb = new StringBuilder();
            for (CollapsedRequest<ProductInfo, Long> request : requests) {
                sb.append(request.getArgument()).append(",");
            }
            String params = sb.toString();
            params = params.substring(0, params.length() - 1);

            Map<String, String> map = new HashMap<>();
            map.put("productIds", params);
            String url = "http://localhost:8082/getProductInfos";
            MyHttpResponse response = MyHttpUtil.doHttpGet(url, map);
            List<ProductInfo> productInfos = JSONArray.parseArray(response.getResponseBody(), ProductInfo.class);
            return productInfos;
        }
    }
}
