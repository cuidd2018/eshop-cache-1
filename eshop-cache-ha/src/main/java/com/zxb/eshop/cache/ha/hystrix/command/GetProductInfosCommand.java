package com.zxb.eshop.cache.ha.hystrix.command;

import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixObservableCommand;
import com.zxb.eshop.cache.ha.http.MyHttpUtil;
import com.zxb.eshop.cache.ha.model.ProductInfo;
import com.zxb.eshop.cache.ha.vo.MyHttpResponse;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xuery on 2018/2/28.
 */
public class GetProductInfosCommand extends HystrixObservableCommand<ProductInfo> {

    private String[] productIds;

    public GetProductInfosCommand(String[] productIds){
        super(HystrixCommandGroupKey.Factory.asKey("GetProductInfoGroup"));
        this.productIds = productIds;
    }

    /**
     * 这个是由调用方的线程来执行的
     * @return
     */
    @Override
    protected Observable<ProductInfo> construct() {
        System.out.println(Thread.currentThread().getName()+" is running...");
        return Observable.create(new Observable.OnSubscribe<ProductInfo>() {
            @Override
            public void call(Subscriber<? super ProductInfo> observer) {
                try{
                    for(String productId: productIds){
                        String url = "http://localhost:8082/getProductInfo";
                        Map<String, String> map = new HashMap<>();
                        map.put("productId", String.valueOf(productId));
                        MyHttpResponse response = MyHttpUtil.doHttpGet(url, map);
                        ProductInfo productInfo = JSONObject.parseObject(response.getResponseBody(), ProductInfo.class);
                        observer.onNext(productInfo);
                    }
                    observer.onCompleted();
                }catch (Exception e){
                    observer.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io());
    }
}
