package com.zxb.eshop.product.ha.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by xuery on 2018/2/28.
 */
@Controller
public class ProductController {

    @RequestMapping("/getProductInfo")
    @ResponseBody
    public String getProductInfo(Long productId){
        return "{\"id\":"+productId+",\"name\":\"iphone7手机\",\"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", \"service\": " +
                "\"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", \"size\": \"5.5\", \"shopId\": 1, \"modifiedTime\": \"2017-01-01 12:00:00\",\"cityId\":1," +
                "\"brandId\":1}";
    }

    @RequestMapping("/getProductInfos")
    @ResponseBody
    public String getProductInfos(String productIds){
        System.out.println("getProductInfos params="+productIds);
        String[] productIdArr = productIds.split(",");
        JSONArray jsonArray = new JSONArray();
        for(String productId : productIdArr){
            String productInfoJson = "{\"id\":"+productId+",\"name\":\"iphone7手机\",\"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", \"service\": " +
                    "\"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", \"size\": \"5.5\", \"shopId\": 1, \"modifiedTime\": \"2017-01-01 12:00:00\",\"cityId\":1," +
                    "\"brandId\":1}";
            jsonArray.add(JSONObject.parseObject(productInfoJson));
        }
        return jsonArray.toJSONString();
    }
}
