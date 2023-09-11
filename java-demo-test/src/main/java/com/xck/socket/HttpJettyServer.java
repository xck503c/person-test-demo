package com.xck.socket;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.util.CharsetUtil;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HttpJettyServer extends AbstractHandler {

    private static String ssl_password = "sms20141207" ;
    private static String ssl_key_password = "sms20141207" ;

    public static void main(String[] args) throws Exception {

        SelectChannelConnector selectChannelConnector = new SelectChannelConnector();
        selectChannelConnector.setPort(8888);
        selectChannelConnector.setMaxIdleTime(60000);// 连接最大空闲时间单位毫秒(ms)
        selectChannelConnector.setAcceptors(4);// 处理请求的cpu数，一般设置为cpu数+1
        selectChannelConnector.setAcceptQueueSize(3000);// 连接被accpet前允许等待的连接数默认50
        selectChannelConnector.setThreadPool(new QueuedThreadPool(40));
        selectChannelConnector.setLowResourcesConnections(100);// 达到lowResourcesConnections连接数时认为资源耗尽
        selectChannelConnector.setLowResourcesMaxIdleTime(100);
        selectChannelConnector.setRequestHeaderSize(1024 * 1024);
        selectChannelConnector.setRequestBufferSize(1024 * 1024);

        String path = "D://" + File.separator + "keystore";

        SslSelectChannelConnector selectChannelConnectorSSL = new SslSelectChannelConnector();
        selectChannelConnectorSSL.setPort(8889);
        selectChannelConnectorSSL.setMaxIdleTime(6000000);// 连接最大空闲时间
        selectChannelConnectorSSL.setAcceptors(4);
        selectChannelConnectorSSL.setAcceptQueueSize(3000);// 连接被accpet前允许等待的连接数默认50
        selectChannelConnectorSSL.setThreadPool(new QueuedThreadPool(40));
        selectChannelConnectorSSL.setLowResourcesConnections(100);// 达到lowResourcesConnections连接数时认为资源耗尽
        selectChannelConnectorSSL.setLowResourcesMaxIdleTime(100);
        selectChannelConnectorSSL.setRequestHeaderSize(1024 * 1024);
        selectChannelConnectorSSL.setRequestBufferSize(1024 * 1024);
        selectChannelConnectorSSL.setKeystore(path);
        selectChannelConnectorSSL.setPassword(ssl_password);
        selectChannelConnectorSSL.setKeyPassword(ssl_key_password);
        selectChannelConnectorSSL.setAllowRenegotiate(true);
        selectChannelConnectorSSL.setWantClientAuth(false);


        Server server = new Server();
        server.setSendServerVersion(false);
        server.setConnectors(new Connector[]{selectChannelConnector,selectChannelConnectorSSL});

        server.setHandler(new HttpJettyServer());
        server.start();
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            String content = readJson(request);

            //JSONObject json = JSON.parseObject(content);
            System.out.println(content);
//            List<String> list = new ArrayList<>();
//            list.add(content);
//            FileUtil.appendLines(list, new File("D:/wzyh-report-1.txt"), CharsetUtil.UTF_8);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", 0);
            jsonObject.put("errMsg", "ok");
            response.getWriter().print(jsonObject.toJSONString());
            baseRequest.setHandled(true);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
            response.getWriter().print("500");
            baseRequest.setHandled(true);
        } catch (Throwable e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
            response.getWriter().print("500");
            baseRequest.setHandled(true);
        }
    }

    /**
     * 读json请求信息
     *
     * @param request
     * @return
     * @throws IOException
     */
    public static String readJson(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            char[] buffer = new char[4096];
            br = new BufferedReader(new InputStreamReader(request.getInputStream(), "utf-8"));
            int i = -1;
            while ((i = br.read(buffer)) != -1) {
                sb.append(buffer, 0, i);
            }
            return sb.toString();
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }
}
