package com.xck.socket.servletcontainer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

public class SimpleServletContext implements ServletContext {

    private String contextPath;

    private String warPath;

    private String tempDir;

    private WebappClassLoader webappClassLoader;

    /**
     * servetlname->servlet对应的对象
     */
    private Map<String, Servlet> servletMap = new HashMap<>();
    private Map<Pattern,  String> servletMapping = new HashMap<>();

    /**
     * servetlname-filter对应的对象
     */
    private Map<String, Filter> filterMap = new HashMap<>();
    private Map<Pattern,  String> filtertMapping = new HashMap<>();

    private Map<String, Object> attributeMap = new ConcurrentHashMap<>();

    private Map<String, String> initParams = new HashMap<>();

    public SimpleServletContext(String contextPath, String warPath, String tempDir) {
        this.contextPath = contextPath;
        this.warPath = warPath;
        this.tempDir = tempDir;
        this.webappClassLoader = new WebappClassLoader();
    }

    public void init() throws Exception{
        // 初始化tempDir路径
        File warFile = new File(warPath);
        if (tempDir == null) {
            tempDir = warFile.getParentFile().getAbsolutePath();
        }

        // 不存在就创建
        File tempFile = new File(tempDir);
        if (!tempFile.exists()) {
            tempFile.mkdirs();
        }
        if (tempFile.isFile()) {
            throw new RuntimeException("tempDir is file");
        }

        unZipWar(warPath, tempDir);

        // 将指定jar和class加入到
        File libDir = new File(tempDir, "WEB-INF/lib");
        for (File jarFile : libDir.listFiles()) {
            if (jarFile.getName().endsWith(".jar")) {
                webappClassLoader.addURL(jarFile.toURI().toURL());
            }
        }
        webappClassLoader.addURL(new File(tempDir, "WEB-INF/classes").toURI().toURL());

        parseWebxml();
    }

    private void unZipWar(String warPath, String tempDir) {
        // 解压缩到指定目录
        try (ZipFile zipFile = new ZipFile(warPath)) {
            zipFile.stream().forEach(zipEntry -> {
                File entryDest = new File(tempDir, zipEntry.getName());
                // 跳过
                if (entryDest.exists()) {
                    return;
                }

                if (zipEntry.isDirectory()) {
                    entryDest.mkdirs();
                } else {
                    // 创建目录
                    entryDest.getParentFile().mkdirs();
                    // 解压数据
                    try (InputStream is = zipFile.getInputStream(zipEntry);
                         FileOutputStream fileOutputStream = new FileOutputStream(entryDest)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = is.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, len);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseWebxml() throws Exception {

        File webXmlFile = new File(tempDir, "WEB-INF/web.xml");

        Document document = Jsoup.parse(webXmlFile, "UTF-8");

        // 解析servlet标签
        Elements nodeList = document.getElementsByTag("servlet");
        for (Element element : nodeList) {
            String servletName = element.getElementsByTag("servlet-name").text();
            String servletClass = element.getElementsByTag("servlet-class").text();
            Servlet instance = (Servlet) webappClassLoader.loadClass(servletClass).newInstance();
            instance.init(new SimpleServletConfig(servletName, this));
            servletMap.put(servletName, instance);
        }

        // 解析filter标签
        nodeList = document.getElementsByTag("filter");
        for (Element element : nodeList) {
            String filterName = element.getElementsByTag("filter-name").text();
            String filterClass = element.getElementsByTag("filter-class").text();
            Filter instance = (Filter) webappClassLoader.loadClass(filterClass).newInstance();
            instance.init(new SimpleFilterConfig(filterName, this));
            filterMap.put(filterName, instance);
        }

        // 解析mapping
        nodeList = document.getElementsByTag("servlet-mapping");
        for (Element element : nodeList) {
            String servletName = element.getElementsByTag("servlet-name").text();
            String urlPattern = element.getElementsByTag("url-pattern").text();
            this.servletMapping.put(urlPattern(urlPattern), servletName);
        }

        nodeList = document.getElementsByTag("filter-mapping");
        for (Element element : nodeList) {
            String filterName = element.getElementsByTag("filter-name").text();
            String urlPattern = element.getElementsByTag("url-pattern").text();
            this.filtertMapping.put(urlPattern(urlPattern), filterName);
        }
    }

    private Pattern urlPattern(String urlPattern) {
        // 将.进行转义，避免触发正则匹配
        urlPattern = urlPattern.replaceAll("\\.", "\\\\.");
        // 将问号替换成点，表示只匹配一个字符
        urlPattern = urlPattern.replaceAll("\\?", ".");
        // 将连续的2个以上的*替换成.* 表示可以匹配任意目录层级
        urlPattern = urlPattern.replaceAll("\\*{2,}", ".*");
        // *表示可以匹配除/外的任意字符
        urlPattern = urlPattern.replaceAll("\\*", "[^/]*");
        return Pattern.compile(urlPattern);
    }

    public Filter getFilterByUri(String uri) {
        for (Map.Entry<Pattern, String> entry : filtertMapping.entrySet()) {
            // 完全匹配
            if (entry.getKey().matcher(uri).matches()) {
                return filterMap.get(entry.getValue());
            }
        }

        return null;
    }

    public Servlet getServletByUri(String uri) {
        for (Map.Entry<Pattern, String> entry : servletMapping.entrySet()) {
            // 完全匹配
            if (entry.getKey().matcher(uri).matches()) {
                return servletMap.get(entry.getValue());
            }
        }

        return null;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public ServletContext getContext(String uripath) {
        System.out.println("getContext");
        return null;
    }

    @Override
    public int getMajorVersion() {
        return 3;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return 3;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return 0;
    }

    @Override
    public String getMimeType(String file) {
        System.out.println("getMimeType");
        return null;
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        System.out.println("getResourcePaths");
        return null;
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        System.out.println("getResource");
        return null;
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        System.out.println("getResourceAsStream" + path);
        return null;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        System.out.println("getRequestDispatcher");
        return null;
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        System.out.println("getNamedDispatcher");
        return null;
    }

    @Override
    public Servlet getServlet(String name) throws ServletException {
        return servletMap.get(name);
    }

    @Override
    public Enumeration<Servlet> getServlets() {
        return Collections.enumeration(servletMap.values());
    }

    @Override
    public Enumeration<String> getServletNames() {
        return Collections.enumeration(servletMap.keySet());
    }

    @Override
    public void log(String msg) {

    }

    @Override
    public void log(Exception exception, String msg) {

    }

    @Override
    public void log(String message, Throwable throwable) {

    }

    @Override
    public String getRealPath(String path) {
        System.out.println("getRealPath");
        return null;
    }

    @Override
    public String getServerInfo() {
        System.out.println("getServerInfo");
        return null;
    }

    @Override
    public String getInitParameter(String name) {
        return initParams.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(initParams.keySet());
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        initParams.put(name, value);
        return true;
    }

    @Override
    public Object getAttribute(String name) {
        return attributeMap.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributeMap.keySet());
    }

    @Override
    public void setAttribute(String name, Object object) {
        attributeMap.put(name, object);
    }

    @Override
    public void removeAttribute(String name) {
        attributeMap.remove(name);
    }

    @Override
    public String getServletContextName() {
        return "name";
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        System.out.println("addServlet");
        return null;
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        System.out.println("addServlet");
        return null;
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        System.out.println("addServlet");
        return null;
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        System.out.println("createServlet");
        return null;
    }

    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        System.out.println("getServletRegistration");
        return null;
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        System.out.println("getServletRegistrations");
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        System.out.println("addFilter");
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        System.out.println("addFilter");
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        System.out.println("addFilter");
        return null;
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        System.out.println("createFilter");
        return null;
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        System.out.println("getFilterRegistration");
        return null;
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        System.out.println("getFilterRegistrations");
        return null;
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        System.out.println("getSessionCookieConfig");
        return null;
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
        System.out.println("setSessionTrackingModes");
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        System.out.println("getDefaultSessionTrackingModes");
        return null;
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        System.out.println("getEffectiveSessionTrackingModes");
        return null;
    }

    @Override
    public void addListener(String className) {
        System.out.println("添加监听器" + className);
    }

    @Override
    public <T extends EventListener> void addListener(T t) {
        System.out.println("添加监听器" + t);
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        System.out.println("添加监听器" + listenerClass);
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        System.out.println("创建监听器" + clazz);
        return null;
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        System.out.println("getJspConfigDescriptor");
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return webappClassLoader;
    }

    @Override
    public void declareRoles(String... roleNames) {
        System.out.println("declareRoles");
    }
}
