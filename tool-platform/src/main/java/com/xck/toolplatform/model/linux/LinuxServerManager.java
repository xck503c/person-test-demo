package com.xck.toolplatform.model.linux;

import com.alibaba.fastjson.JSONObject;
import com.xck.toolplatform.dao.linux.LinuxServerDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LinuxServerManager {

    @Autowired
    private LinuxServerDao linuxServerDao;

    public void create(XShellConfig xShellConfig) {
        linuxServerDao.create(xShellConfig);
    }

    public void deleteById(Long id) {
        linuxServerDao.deleteById(id);
    }

    public List<XShellConfig> queryAll() {
        return linuxServerDao.getAll();
    }

    public XShellConfig query(Long id) {
        return linuxServerDao.getById(id);
    }

    public void update(XShellConfig xShellConfig) {
        linuxServerDao.updateById(xShellConfig);
    }
}
