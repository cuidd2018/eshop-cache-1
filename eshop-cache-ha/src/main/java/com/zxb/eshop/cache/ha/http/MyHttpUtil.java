package com.zxb.eshop.cache.ha.http;

import com.zxb.eshop.cache.ha.vo.MyHttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by 01368080 on 2017/11/10.
 */
public class MyHttpUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyHttpUtil.class);
    private static final int SOCKET_TIME_OUT = 20000;
    private static final int CONNECT_TIME_OUT = 20000;


    /**
     * @param url
     * @param paramsMap
     * @return
     * @throws URISyntaxException
     * @throws IOException
     * @description: httpGet请求
     * @author: Wind-spg
     */
    public static MyHttpResponse doHttpGet(String url, Map<String,String> paramsMap) {

        List<NameValuePair> params = new ArrayList<>();
        for(Map.Entry<String,String> entry : paramsMap.entrySet()){
            params.add(new BasicNameValuePair(entry.getKey(),entry.getValue()));
        }

        // for version 4.3+
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            // 1.1 创建HttpGet并设置请求参数
            URIBuilder uri = new URIBuilder(url);
            uri.setParameters(params);
            HttpGet httpGet = new HttpGet(uri.build());

            // 1.2 设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(SOCKET_TIME_OUT).setConnectTimeout(CONNECT_TIME_OUT).build();
            httpGet.setConfig(requestConfig);

            // 2. 使用httpClient发送请求
            HttpResponse httpResponse = httpClient.execute(httpGet);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                LOGGER.error("Method failed:" + httpResponse.getStatusLine());
            }

            // 3. 获取返回数据
            HttpEntity entity = httpResponse.getEntity();
            String body = EntityUtils.toString(entity, Charset.forName("UTF-8"));
            if (entity != null) {
                EntityUtils.consume(entity);
            }
            LOGGER.info(String.format("do http get end:%s", System.currentTimeMillis()));
            return new MyHttpResponse(statusCode, body, httpGet.getURI().toString());
        } catch (Exception e) {
            LOGGER.error("do http get error!", e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                LOGGER.error("close httpclient error!", e);
            }
        }
        return null;
    }

    /**
     * @param url
     * @param paramsMap
     * @return
     * @throws IOException
     * @description: 发送httpPost请求
     * @author: Wind-spg
     */
    public static MyHttpResponse doHttpPost(String url, Map<String, String> paramsMap) {

        LOGGER.info(String.format("do http post start:%s", System.currentTimeMillis()));

        List<NameValuePair> params = new ArrayList<>();
        for(Map.Entry<String,String> entry : paramsMap.entrySet()){
            params.add(new BasicNameValuePair(entry.getKey(),entry.getValue()));
        }

        // for version4.3+
        CloseableHttpClient httpClient = HttpClients.createDefault();

        // 1.1 创建httpPost并设置请求参数
        HttpPost httpPost = new HttpPost(url);
        UrlEncodedFormEntity postEntity = new UrlEncodedFormEntity(params, Charset.forName("UTF-8"));
        httpPost.setEntity(postEntity);

        // 1.2 设置请求和传输超时时间
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(SOCKET_TIME_OUT).setConnectTimeout(CONNECT_TIME_OUT).build();
        httpPost.setConfig(requestConfig);
        try {
            // 2. 使用httpClient发送post请求
            HttpResponse httpResponse = httpClient.execute(httpPost);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                LOGGER.error("Method failed:" + httpResponse.getStatusLine());
            }

            HttpEntity entity = httpResponse.getEntity();
            String result = EntityUtils.toString(entity, Charset.forName("UTF-8"));
            if (entity != null) {
                EntityUtils.consume(entity);
            }
            LOGGER.debug(String.format("http post response:%s", result));
            LOGGER.info(String.format("do http post end:%s", System.currentTimeMillis()));
            return new MyHttpResponse(statusCode, result, httpPost.getURI().toString());
        } catch (IOException e) {
            LOGGER.error("do http post error!", e);
        } finally {
            try {
                // 关闭流并释放资源,for version4.3+
                httpClient.close();
            } catch (IOException e) {
                LOGGER.error("close httpclient error!", e);
            }
        }
        return null;
    }
}
