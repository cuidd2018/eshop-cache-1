package com.zxb.eshop.inventory.service;

import com.zxb.eshop.inventory.request.Request;

/**
 * Created by xuery on 2018/1/23.
 */
public interface RequestAsyncProcessService {

    void process(Request request);
}
