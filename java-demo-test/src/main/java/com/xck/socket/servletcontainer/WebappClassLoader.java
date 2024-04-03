package com.xck.socket.servletcontainer;

import java.net.URL;
import java.net.URLClassLoader;

public class WebappClassLoader extends URLClassLoader {

    public WebappClassLoader() {
        super(new URL[]{});
    }

    public WebappClassLoader(ClassLoader parent) {
        super(new URL[]{}, parent);
    }

    // 动态加载
    public void addURL(URL url) {
        super.addURL(url);
    }
}
