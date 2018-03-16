package com.zxb.eshop.cache.ha;

import com.zxb.eshop.cache.ha.Utils.SleepUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by xuery on 2018/3/8.
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class CyclicBarrierTest {

    /**
     * CyclicBarrier的作用：用于多线程计算，然后合并计算结果，提升效率
     */
    @Test
    public void testCyclicBarrier(){
        CyclicBarrier cyclicBarrier = new CyclicBarrier(10, new Runnable() {
            @Override
            public void run() {
                System.out.println("等人都到齐。。。");
            }
        });

        for (int i = 0; i < 10; i++) {
            int index = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getName() + ",index=" + index);
                    try {
                        cyclicBarrier.await();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        SleepUtils.sleep(5000);
    }
}
