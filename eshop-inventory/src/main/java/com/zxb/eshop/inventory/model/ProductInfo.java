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
	private String pictureList;
	private String specification;
	private String service;
	private String color;
	private String size;
	private Long shopId;
	
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

	public String getPictureList() {
		return pictureList;
	}

	public void setPictureList(String pictureList) {
		this.pictureList = pictureList;
	}

	public String getSpecification() {
		return specification;
	}

	public void setSpecification(String specification) {
		this.specification = specification;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public Long getShopId() {
		return shopId;
	}

	public void setShopId(Long shopId) {
		this.shopId = shopId;
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
		sb.append(",\"pictureList\":\"")
				.append(pictureList).append('\"');
		sb.append(",\"specification\":\"")
				.append(specification).append('\"');
		sb.append(",\"service\":\"")
				.append(service).append('\"');
		sb.append(",\"color\":\"")
				.append(color).append('\"');
		sb.append(",\"size\":\"")
				.append(size).append('\"');
		sb.append(",\"shopId\":")
				.append(shopId);
		sb.append('}');
		return sb.toString();
	}
}
