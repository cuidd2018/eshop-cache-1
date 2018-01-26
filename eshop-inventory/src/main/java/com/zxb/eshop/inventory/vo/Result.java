package com.zxb.eshop.inventory.vo;

import java.io.Serializable;

/**
 * 请求的响应
 * Created by xuery on 2018/1/23.
 */
public class Result<T> implements Serializable{

    private static final long serialVersionUID = 1L;

    private boolean success = false;
    private String message;
    private String errorCode;
    private String errorMsg;
    private T obj;

    public Result() {
    }

    public Result(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public T getObj() {
        return obj;
    }

    public void setObj(T obj) {
        this.obj = obj;
    }
}
