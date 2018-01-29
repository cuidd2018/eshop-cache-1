package com.zxb.eshop.inventory;

import com.zxb.eshop.inventory.request.ProductInventoryCacheRefreshRequest;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EshopInventoryApplicationTests {

	@Test
	public void contextLoads() {
	}

	@Test
	public void test(){
		String json = "{\"name\":\"zhangsan\",\"age\":25}";
		System.out.println(">>>>>>>>>>"+json);
	}
}
