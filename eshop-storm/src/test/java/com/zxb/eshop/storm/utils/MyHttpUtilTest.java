package com.zxb.eshop.storm.utils;

import com.zxb.eshop.storm.vo.MyHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 01368080 on 2017/11/10.
 */
public class MyHttpUtilTest {

    private static final Logger logger = LoggerFactory.getLogger(MyHttpUtilTest.class);

    @Test
    public void testHttpGetOrPost() {
        String url = "http://10.118.52.120:8080/getProductInfo";
        Map<String, String> map = new HashMap<>();
        map.put("productId", "4");
        MyHttpResponse response = MyHttpUtil.doHttpGet(url, map);
        String reponse = response.getResponseBody();
        System.out.println(">>>>>>doHttpGet getProductInfo responseBody:" + reponse);
    }
}
