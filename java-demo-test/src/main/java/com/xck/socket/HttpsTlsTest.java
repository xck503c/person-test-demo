package com.xck.socket;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.crypto.tls.DefaultTlsClient;
import org.bouncycastle.crypto.tls.TlsUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.Security;
import java.util.Arrays;
import java.util.Map;

/**
 * https的tls版本测试
 *
 * @author xuchengkun
 * @date 2021/09/25 12:05
 **/
public class HttpsTlsTest {

    public static void main(String[] args) {
//        String url = "https://asyn-notice-gateway.yihuitong.top/notice/sms/baiwu/send/notice";
//        String url = "https://172.17.114.220:8445/push_report.do";
        String url = "https://127.0.0.1:8888/push_report.do";
//        String url = "https://127.0.0.1:8098/push_report.do";
        System.out.println(doHttpsWithHeaderMap(url, "POST", "aaaa", "UTF-8", "application/xml"));
//        System.out.println(jsonPostSend(url, "aaaa", "UTF-8", "application/xml"));

    }

    public static String doHttpsWithHeaderMap(String url, String GETOrPOST, String param, String charset, String content_type) {
        Security.insertProviderAt(new BouncyCastleProvider(),1);
        StringBuffer buffer = new StringBuffer();
        HttpsURLConnection httpUrlConn = null;

        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化
            TrustManager[] tm = { new MyTrustManager() };
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, tm, new java.security.SecureRandom());
            //从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();

//            System.out.println("ssl默认使用协议: " + sslContext.getProtocol());
//            SSLEngine sslEngine = sslContext.createSSLEngine();
//            System.out.println("支持的协议: " + Arrays.asList(sslEngine.getSupportedProtocols()));
//            System.out.println("支持的加密套件: " + Arrays.asList(sslEngine.getSupportedCipherSuites()));
//            System.out.println("启用的加密套件: " + Arrays.asList(sslEngine.getEnabledCipherSuites()));

            httpUrlConn = (HttpsURLConnection) new URL(url).openConnection();
//            httpUrlConn.setSSLSocketFactory(ssf);
            httpUrlConn.setSSLSocketFactory(new TLSSocketConnectionFactory());
            httpUrlConn.setRequestProperty("Content-Type", ""+content_type+"; charset="+charset);
            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);
            httpUrlConn.setConnectTimeout(10000);
            httpUrlConn.setReadTimeout(10000);
            // 设置请求方式（GET/POST）
            httpUrlConn.setRequestMethod(GETOrPOST);

            //不校验证书
//            httpUrlConn.setHostnameVerifier(new HostnameVerifier() {
//                @Override
//                public boolean verify(String s, SSLSession sslSession) {
//                    return true;
//                }
//            });

            // 兼容有数据提交、无数据提交两种情况
            // 当有数据需要提交时
            if (!param.equals("")&&null!=param) {
                OutputStream outputStream = httpUrlConn.getOutputStream();
                outputStream.write(param.getBytes(charset));
                outputStream.close();
            }

            // 将返回的输入流转换成字符串
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }

            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();

        }catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                httpUrlConn.disconnect();
            } catch (Exception e2) {
            }
        }

        return buffer.toString();
    }

    public static String jsonPostSend(String url, String data, String charset, String contentType) {
        String result = "未知";
        if(StringUtils.isBlank(url)) {
            result = "no_url_adress";
            return result;
        }

        HttpClient httpClient = new HttpClient();
        HttpConnectionManagerParams managerParams = httpClient.getHttpConnectionManager().getParams();
        //设置连接超时，如url不存在
        managerParams.setConnectionTimeout(10000);
        //设置读数据超时，超过时间未响应，报异常
        managerParams.setSoTimeout(10000);
        PostMethod method = new PostMethod(url);
        RequestEntity requestEntity = null;
        try {
            requestEntity = new StringRequestEntity(data, contentType, charset);
            method.setRequestEntity(requestEntity);
            httpClient.executeMethod(method);
            result = method.getResponseBodyAsString();
        } catch (Exception e) {
            result = e.getMessage();
        } finally {
            method.releaseConnection();
            try {
                ((SimpleHttpConnectionManager) httpClient.getHttpConnectionManager()).shutdown();
            } catch (Exception e2) {
                result = e2.getMessage();
            }
        }
        return result;
    }
}
