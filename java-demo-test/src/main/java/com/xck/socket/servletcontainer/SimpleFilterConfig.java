package com.xck.socket.servletcontainer;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

public class SimpleFilterConfig implements FilterConfig {

    private String filterName;

    private ServletContext servletContext;

    public SimpleFilterConfig(String filterName, ServletContext servletContext) {
        this.filterName = filterName;
        this.servletContext = servletContext;
    }

    @Override
    public String getFilterName() {
        return filterName;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return null;
    }
}
