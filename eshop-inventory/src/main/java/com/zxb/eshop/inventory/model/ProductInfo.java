package com.zxb.eshop.inventory.model;

/**
 * 商品信息类
 * @author xuery
 *
 */
public class ProductInfo {

	private Long id;
	private String name;
	private Double price;
	
	public ProductInfo() {
		
	}
	
	public ProductInfo(Long id, String name, Double price) {
		this.id = id;
		this.name = name;
		this.price = price;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("{");
		sb.append("\"id\":")
				.append(id);
		sb.append(",\"name\":\"")
				.append(name).append('\"');
		sb.append(",\"price\":")
				.append(price);
		sb.append('}');
		return sb.toString();
	}
}
