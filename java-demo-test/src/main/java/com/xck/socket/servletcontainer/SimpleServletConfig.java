package com.xck.socket.servletcontainer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

public class SimpleServletConfig implements ServletConfig {

    private String servletName;

    private ServletContext servletContext;

    public SimpleServletConfig(String servletName, ServletContext servletContext) {
        this.servletName = servletName;
        this.servletContext = servletContext;
    }

    @Override
    public String getServletName() {
        return servletName;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(String s) {
        return getServletContext().getInitParameter(s);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return getServletContext().getInitParameterNames();
    }
}
