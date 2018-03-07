package com.zxb.eshop.cache.ha;

import com.zxb.eshop.cache.ha.Utils.SleepUtils;
import com.zxb.eshop.cache.ha.http.MyHttpUtil;
import com.zxb.eshop.cache.ha.hystrix.command.GetProductInfoCommand;
import com.zxb.eshop.cache.ha.vo.MyHttpResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by xuery on 2018/3/1.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RejectTest {

    /**
     * 并发请求超过线程池+等待队列个数时，测试是否会采用拒绝策略
     *
     * 这里死活打印不出结果，其实是执行了，暂时还不知道原因。。。
     *原因：Junit @Test其实不是严格支持多线程的，当主线程执行完时，整个程序就会结束，不会等待子线程执行完才结束
     */
    @Test
    public void testReject() {
        CountDownLatch countDownLatch = new CountDownLatch(30);
        for (int i = 0; i < 30; i++) {
            int index = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    GetProductInfoCommand getProductInfoCommand = new GetProductInfoCommand(-2L);
                    System.out.println("第" + (index + 1) + "次商品查询结果为：" + getProductInfoCommand.execute());
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
        } catch (Exception e){
            e.printStackTrace();
        }
//        SleepUtils.sleep(20000);
    }
}
