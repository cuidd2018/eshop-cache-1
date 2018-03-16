package com.zxb.eshop.cache.ha.cache.local;

import java.util.HashMap;
import java.util.Map;

/**
 * 品牌缓存
 * @author Administrator
 *
 */
public class BrandCache {

	private static Map<Long, String> brandMap = new HashMap<Long, String>();
	private static Map<Long, Long> brandIds = new HashMap<Long, Long>();
	
	static {
		brandMap.put(1L, "iphone");

		brandIds.put(1L, 1L);
	}
	
	public static String getBrandName(Long brandId) {
		return brandMap.get(brandId);
	}

	public static Long getBrandId(Long productId){
		return brandIds.get(productId);
	}
	
}
