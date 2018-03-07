package com.zxb.eshop.cache.ha;

import com.zxb.eshop.cache.ha.hystrix.command.GetProductInfoCommand;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by xuery on 2018/3/2.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TimeoutTest {

    @Test
    public void testTimeout() {
        GetProductInfoCommand getProductInfoCommand = new GetProductInfoCommand(-3L);
        System.out.println("查询商品信息返回结果为：" + getProductInfoCommand.execute());
    }
}
