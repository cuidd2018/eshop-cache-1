package com.zxb.eshop.inventory.model;

/**
 * 测试用户Model类
 * @author xuery
 *
 */
public class User {

	/**
	 * 测试用户姓名
	 */
	private String name;
	/**
	 * 测试用户年龄
	 */
	private Integer age;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("{");
		sb.append("\"name\":\"")
				.append(name).append('\"');
		sb.append(",\"age\":")
				.append(age);
		sb.append('}');
		return sb.toString();
	}
}
