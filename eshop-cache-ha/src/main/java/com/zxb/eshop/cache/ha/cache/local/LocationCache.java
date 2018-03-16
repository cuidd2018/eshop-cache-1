package com.zxb.eshop.cache.ha.cache.local;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xuery on 2018/2/28.
 */
public class LocationCache {

    private static Map<Long, String> cityMap = new HashMap<>();
    private static Map<Long, Long> cityIds = new HashMap<>();

    static{
        cityMap.put(1L, "北京");

        cityIds.put(1L, 755L);
    }

    public static String getCityName(Long cityId){
        return cityMap.get(cityId);
    }

    public static Long getCityId(Long productId){
        return cityIds.get(productId);
    }
}
