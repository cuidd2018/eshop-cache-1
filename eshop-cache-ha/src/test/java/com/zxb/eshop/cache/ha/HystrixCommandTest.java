package com.zxb.eshop.cache.ha;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.zxb.eshop.cache.ha.hystrix.command.CommandUsingRequestCache;
import com.zxb.eshop.cache.ha.hystrix.command.GetBrandNameCommand;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by xuery on 2018/3/1.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class HystrixCommandTest {

    /**
     * 测试同一个上下文相同的请求参数是否回去缓存中取值
     */
    @Test
    public void testRequestCache() {
        HystrixRequestContext context = HystrixRequestContext.initializeContext();
        try {
            CommandUsingRequestCache command1 = new CommandUsingRequestCache(2);
            CommandUsingRequestCache command2 = new CommandUsingRequestCache(2);
            Assert.assertTrue(command1.execute());
            Assert.assertFalse(command1.isResponseFromCache());

            Assert.assertTrue(command2.execute());
            Assert.assertTrue(command2.isResponseFromCache());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            context.shutdown();
        }

        //开启一个新的上下文
        HystrixRequestContext context1 = HystrixRequestContext.initializeContext();
        try {
            CommandUsingRequestCache command3 = new CommandUsingRequestCache(2);
            Assert.assertTrue(command3.execute());
            Assert.assertFalse(command3.isResponseFromCache());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            context1.shutdown();
        }
    }

    @Test
    public void testFallbackOfException() {
        Long brandId = 1L;
        GetBrandNameCommand getBrandNameCommand = new GetBrandNameCommand(brandId);
        String brandName = getBrandNameCommand.execute();
        System.out.println(">>>brandName:" + brandName);
    }

}
