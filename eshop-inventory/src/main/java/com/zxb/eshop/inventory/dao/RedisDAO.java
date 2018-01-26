package com.zxb.eshop.inventory.dao;

/**
 * Created by xuery on 2018/1/23.
 */
public interface RedisDAO {

    public void set(String key, String value);

    public String get(String key);

    public void delete(String key);
}
