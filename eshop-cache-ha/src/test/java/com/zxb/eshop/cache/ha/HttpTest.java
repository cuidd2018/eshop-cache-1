package com.zxb.eshop.cache.ha;

import com.zxb.eshop.cache.ha.http.MyHttpUtil;
import com.zxb.eshop.cache.ha.vo.MyHttpResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

/**
 * Created by xuery on 2018/3/9.
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class HttpTest {

    @Test
    public void testHttpPost(){
        MyHttpResponse response = MyHttpUtil.doHttpPost("http://10.202.7.165:8180/sf-test-platform-web/mock/msdTPPsit/V1.0/rmds/hub/findHubByCityCode", new HashMap<>());
        System.out.println(">>>>responseBody:"+response.getResponseBody());
    }
}
