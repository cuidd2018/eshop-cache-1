package com.zxb.eshop.cache.ha.Utils;

/**
 * Created by xuery on 2018/3/1.
 */
public class SleepUtils {

    public static void sleep(int milliSecond){
        try{
            Thread.sleep(milliSecond);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
