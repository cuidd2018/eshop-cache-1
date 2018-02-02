package com.zxb.eshop.storm.utils;

import java.util.*;

/**
 * Created by xuery on 2018/2/1.
 */
public class MapUtils {

    public static void sortMapByValue(Map<Long, Long> map){
        Set<Map.Entry<Long, Long>> entrySet = map.entrySet();
        for(Map.Entry<Long, Long> entry : entrySet){
            System.out.println("k:"+entry.getKey()+",v:"+entry.getValue());
        }

        List<Map.Entry<Long, Long>> listMap = new LinkedList<>(entrySet);

        Collections.sort(listMap, new Comparator<Map.Entry<Long, Long>>() {
            @Override
            public int compare(Map.Entry<Long, Long> o1, Map.Entry<Long, Long> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        System.out.println(listMap);
    }

    public static void main(String[] args) {
        Map<Long, Long> map = new HashMap<>();
//        map.put(1L,7L);
//        map.put(2L,5L);
//        map.put(3L,3L);
//        map.put(4L,4L);
        sortMapByValue(map);
    }
}
