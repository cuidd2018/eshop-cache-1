package com.zxb.eshop.cache.ha;

import com.zxb.eshop.cache.ha.Utils.SleepUtils;
import com.zxb.eshop.cache.ha.http.MyHttpUtil;
import com.zxb.eshop.cache.ha.hystrix.command.GetProductInfoCommand;
import com.zxb.eshop.cache.ha.vo.MyHttpResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xuery on 2018/3/1.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class CircuitBreakerTest {

    @Test
    public void testCircuitBreaker(){
        //先发15个正常请求
        for(int i=0;i<15;i++){
            GetProductInfoCommand getProductInfoCommand = new GetProductInfoCommand(1L);
            System.out.println("第"+(i+1)+"次商品请求结果为:"+getProductInfoCommand.execute());
        }
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        //再发25个异常请求 则过一段时间断路器应该要打开
        for(int i=0;i<25;i++){
            GetProductInfoCommand getProductInfoCommand = new GetProductInfoCommand(-1L);
            System.out.println("第"+(i+1)+"次商品请求结果为:"+getProductInfoCommand.execute());
        }

        //休眠一段时间，让断路器有时间去统计
        SleepUtils.sleep(5000);

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        //这时候发送异常请求时，断路器应该打开了，对于第一个请求会去看一下能否正常执行，能则关闭断路器；否则后续请求直接降级，不会请求到run方法中
        for(int i=0;i<10;i++){
            GetProductInfoCommand getProductInfoCommand = new GetProductInfoCommand(-1L);
            System.out.println("第"+(i+1)+"次商品请求结果为:"+getProductInfoCommand.execute());
        }

        //再休眠一段时间，让断路器有时间去统计请求是否正常，正常了则关闭断路器
        SleepUtils.sleep(5000);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        //发送正常请求，此时断路器应该会去尝试看能否走正常逻辑；如果这时候继续发送异常请求会出现什么情况呢？试一下
        for(int i=0;i<10;i++){
            GetProductInfoCommand getProductInfoCommand = new GetProductInfoCommand(1L);
            System.out.println("第"+(i+1)+"次商品请求结果为:"+getProductInfoCommand.execute());
        }
    }

}
