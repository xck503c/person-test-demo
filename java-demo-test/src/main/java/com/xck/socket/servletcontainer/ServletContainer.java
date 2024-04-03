package com.xck.socket.servletcontainer;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServletContainer implements Handler {

    private String warPath;

    private String contextPath = "/";

    private String tempDir;

    private SimpleServletContext servletContext;

    public ServletContainer() {
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String uri = baseRequest.getRequestURI();

        // 如果不以指定上下文路径开头，直接返回404
        if (!uri.startsWith(contextPath)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            baseRequest.setHandled(true);
            return;
        } else {
            // 截取，便于后面匹配
            uri = uri.substring(contextPath.length());
            // 这里需要进行处理，传递给后面
            baseRequest.setContextPath(contextPath);
            baseRequest.setPathInfo(uri);
        }

        Filter filter = servletContext.getFilterByUri(uri);
        if (filter != null) {
            filter.doFilter(request, response, null);
        }

        Servlet servlet = servletContext.getServletByUri(uri);
        if (servlet != null) {
            servlet.service(request, response);
        }

        baseRequest.setHandled(true);
    }

    @Override
    public void setServer(Server server) {

    }

    @Override
    public Server getServer() {
        return null;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void start() throws Exception {
        // 解析war包
        servletContext = new SimpleServletContext(contextPath, warPath, tempDir);
        servletContext.init();
    }

    @Override
    public void stop() throws Exception {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public boolean isStarting() {
        return false;
    }

    @Override
    public boolean isStopping() {
        return false;
    }

    @Override
    public boolean isStopped() {
        return false;
    }

    @Override
    public boolean isFailed() {
        return false;
    }

    @Override
    public void addLifeCycleListener(Listener listener) {
        System.out.println("addLifeCycleListener" + listener.getClass().getName() + listener.toString());
    }

    @Override
    public void removeLifeCycleListener(Listener listener) {
        System.out.println("addLifeCycleListener" + listener.getClass().getName() + listener.toString());
    }

    public void setWarPath(String warPath) {
        this.warPath = warPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);

        ServletContainer servletContainer = new ServletContainer();
        servletContainer.setWarPath("D:\\work\\receiver\\sendApi\\webservice-sms-send-V1.0.0.war");
        servletContainer.setTempDir("D:\\work\\receiver\\web_temp\\webservice");
        servletContainer.setContextPath("/webservice");

        server.setHandler(servletContainer);

        server.start();
    }
}
