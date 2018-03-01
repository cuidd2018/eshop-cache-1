package com.zxb.eshop.cache.ha.controller;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixObservableCommand;
import com.zxb.eshop.cache.ha.cache.local.LocationCache;
import com.zxb.eshop.cache.ha.http.MyHttpUtil;
import com.zxb.eshop.cache.ha.hystrix.command.GetBrandNameCommand;
import com.zxb.eshop.cache.ha.hystrix.command.GetCityNameCommand;
import com.zxb.eshop.cache.ha.hystrix.command.GetProductInfoCommand;
import com.zxb.eshop.cache.ha.hystrix.command.GetProductInfosCommand;
import com.zxb.eshop.cache.ha.model.ProductInfo;
import com.zxb.eshop.cache.ha.vo.MyHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import rx.Observable;
import rx.Observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xuery on 2018/2/28.
 */
@Controller
public class CacheController {

    /**
     * 未加Hystrix做资源隔离操作获取商品信息
     *
     * @param productId
     * @return
     */
    @RequestMapping("/change/product")
    @ResponseBody
    public String changeProduct(Long productId) {
        String url = "http://localhost:8082/getProductInfo";
        Map<String, String> map = new HashMap<>();
        map.put("productId", String.valueOf(productId));
        MyHttpResponse response = MyHttpUtil.doHttpGet(url, map);
        System.out.println(">>>>not hystrix productInfo:" + response.getResponseBody());
        return "success";
    }

    /**
     * 模拟nginx各级缓存都失效的场景下，nginx发送很多请求直接请求到当前缓存服务拉取原始数据
     *
     * @param productId
     * @return
     */
    @RequestMapping("/getProductInfo")
    @ResponseBody
    public String getProductInfo(Long productId) {
        HystrixCommand<ProductInfo> getProductInfoCommand = new GetProductInfoCommand(productId);
        //同步调用execute
        ProductInfo productInfo = getProductInfoCommand.execute();

        //根据城市id获取城市名称 利用信号量资源隔离
        Long cityId = productInfo.getCityId();
        GetCityNameCommand getCityNameCommand = new GetCityNameCommand(cityId);
        String cityName = getCityNameCommand.execute();
        productInfo.setCityName(cityName);

        Long brandId = productInfo.getBrandId();
        GetBrandNameCommand getBrandNameCommand = new GetBrandNameCommand(brandId);
        String brandName = getBrandNameCommand.execute();
        productInfo.setBrandName(brandName);

        //异步非阻塞方式，调用queue()直接返回一个future对象
//        Future<ProductInfo> future = getProductInfoCommand.queue();
//        try {
//            Thread.sleep(1000);
//            ProductInfo productInfo1 = future.get();
//            System.out.println("queue productInfo:" + productInfo1);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        System.out.println(">>>>GetProductInfoCommand productInfo:" + productInfo);
        return "success";
    }

    /**
     * 批量查询商品信息
     *
     * @param productIds
     * @return
     */
    @RequestMapping("/getProductInfos")
    @ResponseBody
    public String getProductInfo(String productIds) {
        String[] ids = productIds.split(",");
        HystrixObservableCommand<ProductInfo> getProductInfosCommand = new GetProductInfosCommand(ids);
        List<ProductInfo> productInfoList = new ArrayList<>();
        //异步调用，hot，已经执行过construct方法了
        Observable<ProductInfo> observable = getProductInfosCommand.observe();

        //异步调用获取执行结果
        observable.subscribe(new Observer<ProductInfo>() {
            @Override
            public void onCompleted() {
                System.out.println(">>>>已经获取完所有的商品信息:"+productInfoList);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(ProductInfo productInfo) {
                productInfoList.add(productInfo);
                System.out.println("onNext:" + productInfo);
            }
        });

        //还未执行construct方法
//        Observable<ProductInfo> toObservable = getProductInfosCommand.toObservable();

//        toObservable.subscribe(new Observer<ProductInfo>() { //订阅之后才执行construct方法
//            @Override
//            public void onCompleted() {
//                System.out.println(">>>>已经获取完所有的商品信息:" + productInfoList);
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onNext(ProductInfo productInfo) {
//                productInfoList.add(productInfo);
//                System.out.println("onNext:" + productInfo);
//            }
//        });


        return "success";
    }

}
