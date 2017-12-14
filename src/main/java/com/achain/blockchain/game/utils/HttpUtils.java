package com.achain.blockchain.game.utils;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import lombok.extern.slf4j.Slf4j;

/**
 * @author yujianjian
 * @since 2017-12-13 下午7:09
 */
@Slf4j
public class HttpUtils {


    private final static String FORM_TYPE = "application/x-www-form-urlencoded";
    private final static String JSON_TYPE = "application/json";

    /**
     * form表单提交,广播http方法
     *
     * @param url     接口url
     * @param message json参数
     * @return 结果
     */
    public static String broadcastPost(String url, String message) {
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Content-type", FORM_TYPE);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("message", message));
        //设置参数到请求对象中
        httppost.setEntity(new UrlEncodedFormEntity(params, Charset.forName("UTF-8")));
        return baseHttpRequest(httppost, url, "【broadcastPost】｜POST URL:[{}] 关闭httpclient.close()异常[{}]!");
    }


    public static String postJson(String url, String message) {
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Content-type", JSON_TYPE);
        httppost.setEntity(new StringEntity(message, Charset.forName("UTF-8")));
        return baseHttpRequest(httppost, url, "【postJson】｜POST URL:[{}] 关闭httpclient.close()异常[{}]!");
    }

    public static String get(String url) {
        HttpGet httpGet = new HttpGet(url);
        return baseHttpRequest(httpGet, url, "【get】｜GET URL:[{}] 关闭httpclient.close()异常[{}]!");
    }

    private static String baseHttpRequest(HttpRequestBase httpRequestBase, String url, String msg) {
        String result = null;
        try {
            SSLContext sslcontext = createIgnoreVerifySSL();
            // 设置协议http和https对应的处理socket链接工厂的对象
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslcontext))
                .build();
            PoolingHttpClientConnectionManager
                connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            HttpClients.custom().setConnectionManager(connManager);
            CloseableHttpClient httpclients = HttpClients.custom().setConnectionManager(connManager).build();
            CloseableHttpResponse response = httpclients.execute(httpRequestBase);
            if (null != response) {
                try {
                    result = EntityUtils.toString(response.getEntity(), "UTF-8");
                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        result = null;
                    }
                } finally {
                    response.close();
                }
            }
        } catch (Exception e) {
            log.error("{}|error", url, e);
        } finally {
            releaseConnection(url, httpRequestBase, null != httpRequestBase, msg);
        }
        return result;
    }


    private static void releaseConnection(String url, HttpRequestBase httpRequestBase, boolean notNull, String msg) {
        try {
            if (notNull) {
                httpRequestBase.releaseConnection();
            }
        } catch (Exception e) {
            log.error(msg, url, e.getStackTrace());
        }
    }


    /**
     * 绕过验证
     */
    private static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("SSLv3");

        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                                           String paramString) {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                                           String paramString) {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        sc.init(null, new TrustManager[]{trustManager}, null);
        return sc;
    }

}
