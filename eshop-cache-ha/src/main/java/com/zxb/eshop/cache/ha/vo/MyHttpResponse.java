package com.zxb.eshop.cache.ha.vo;

import java.io.Serializable;

/**
 * Created by 01368080 on 2017/11/10.
 */
public class MyHttpResponse implements Serializable{

    private static final long serialVersionUID = 6381662550803520971L;

    private int responseStatus;

    private String responseBody;

    private String requetUrl;

    public MyHttpResponse()
    {

    }

    public MyHttpResponse(int resonseStatus, String responseBody)
    {
        this.responseStatus = resonseStatus;
        this.responseBody = responseBody;
    }

    public MyHttpResponse(int resonseStatus, String responseBody, String requestUrl)
    {
        this.responseStatus = resonseStatus;
        this.responseBody = responseBody;
        this.requetUrl = requestUrl;
    }

    public int getResponseStatus()
    {
        return responseStatus;
    }

    public void setResponseStatus(int responseStatus)
    {
        this.responseStatus = responseStatus;
    }

    public String getResponseBody()
    {
        return responseBody;
    }

    public void setResponseBody(String responseBody)
    {
        this.responseBody = responseBody;
    }

    public String getRequetUrl()
    {
        return requetUrl;
    }

    public void setRequetUrl(String requetUrl)
    {
        this.requetUrl = requetUrl;
    }

    @Override
    public String toString()
    {
        return "MyHttpResponse [responseStatus=" + responseStatus + ", responseBody=" + responseBody + ", requetUrl="
                + requetUrl + "]";
    }

}
