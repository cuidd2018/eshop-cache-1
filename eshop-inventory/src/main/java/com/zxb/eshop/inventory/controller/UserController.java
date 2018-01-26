package com.zxb.eshop.inventory.controller;

import com.zxb.eshop.inventory.model.User;
import com.zxb.eshop.inventory.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by xuery on 2018/1/23.
 */
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/getUserInfo")
    @ResponseBody
    public User getUserInfo(){
        return userService.getUserInfo();
    }

    @RequestMapping("getCachedUserInfo")
    @ResponseBody
    public User getCachedUserInfo(){
        User user = userService.getCachedUserInfo();
        return user;
    }
}
