package com.zxb.eshop.inventory.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.zxb.eshop.inventory.dao.RedisDAO;
import com.zxb.eshop.inventory.model.User;
import com.zxb.eshop.inventory.mapper.UserMapper;
import com.zxb.eshop.inventory.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xuery on 2018/1/23.
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisDAO redisDAO;

    @Override
    public User getUserInfo() {
        return userMapper.getUserInfo();
    }

    @Override
    public User getCachedUserInfo() {
        redisDAO.set("cached_user", "{\"name\":\"zhangsan\",\"age\":25}");
        String json = redisDAO.get("cached_user");
        JSONObject jsonObject = JSONObject.parseObject(json);
        User user = new User();
        user.setName(jsonObject.getString("name"));
        user.setAge(jsonObject.getInteger("age"));

        return user;
    }
}
