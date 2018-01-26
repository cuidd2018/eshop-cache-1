package com.zxb.eshop.inventory.service;

import com.zxb.eshop.inventory.model.User;

/**
 * Created by xuery on 2018/1/23.
 */
public interface UserService {

    public User getUserInfo();

    public User getCachedUserInfo();
}
