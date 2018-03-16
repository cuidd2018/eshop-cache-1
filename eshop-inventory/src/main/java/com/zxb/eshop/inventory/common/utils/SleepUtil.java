package com.zxb.eshop.inventory.common.utils;

/**
 * Created by xuery on 2018/1/29.
 */
public class SleepUtil {

    public static void pause(long milliSeconds){
        try{
            Thread.sleep(milliSeconds);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
