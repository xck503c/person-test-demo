package com.xck.toolplatform.dao.linux;

import com.xck.toolplatform.model.linux.XShellConfig;

import java.util.List;

/**
 * linux服务器信息
 * @author xuchengkun
 */
public interface LinuxServerDao {

    int create(XShellConfig xShellConfig);
    List<XShellConfig> getAll();
    XShellConfig getById(Long id);
    int deleteById(Long id);
}
