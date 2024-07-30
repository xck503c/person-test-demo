package com.xck.toolplatform.dao.linux;

import cn.hutool.core.date.DateTime;
import com.xck.toolplatform.model.linux.XShellConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;

/**
 * linux服务器信息
 * @author xuchengkun
 */
@Repository
public class LinuxServerJdbcDao implements LinuxServerDao{

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * @param xShellConfig
     * @return
     */
    @Override
    public int create(XShellConfig xShellConfig) {
        DateTime dateTime = new DateTime();
        String msStr = dateTime.toMsStr();
        jdbcTemplate.update("insert into xshell_config (jump_ip,jump_port,jump_pwd,target_ip,docker_id,comment,create_time,modify_time)"
                        + " values(?,?,?,?,?,?,?,?)",
                xShellConfig.getJumpIp(),
                xShellConfig.getJumpPort(),
                xShellConfig.getJumpPwd(),
                xShellConfig.getTargetIp(),
                xShellConfig.getDockerId(),
                xShellConfig.getComment(),
                msStr,
                msStr);
        return 1;
    }

    @Override
    public List<XShellConfig> getAll() {
        return jdbcTemplate.query("select * from xshell_config", (ResultSet rs, int i) -> {
            XShellConfig xShellConfig = new XShellConfig();
            xShellConfig.setId(rs.getLong("id"));
            xShellConfig.setJumpIp(rs.getString("jump_ip"));
            xShellConfig.setJumpPort(rs.getInt("jump_port"));
            xShellConfig.setJumpPwd(rs.getString("jump_pwd"));
            xShellConfig.setTargetIp(rs.getString("target_ip"));
            xShellConfig.setDockerId(rs.getString("docker_id"));
            xShellConfig.setComment(rs.getString("comment"));
            return xShellConfig;
        });
    }

    @Override
    public XShellConfig getById(Long id) {
        List<XShellConfig> list = jdbcTemplate.query("select * from xshell_config where id=?", (ResultSet rs, int i) -> {
            XShellConfig xShellConfig = new XShellConfig();
            xShellConfig.setId(rs.getLong("id"));
            xShellConfig.setJumpIp(rs.getString("jump_ip"));
            xShellConfig.setJumpPort(rs.getInt("jump_port"));
            xShellConfig.setJumpPwd(rs.getString("jump_pwd"));
            xShellConfig.setTargetIp(rs.getString("target_ip"));
            xShellConfig.setDockerId(rs.getString("docker_id"));
            xShellConfig.setComment(rs.getString("comment"));
            return xShellConfig;
        }, id);
        return list.get(0);
    }

    @Override
    public int deleteById(Long id) {
        jdbcTemplate.update("delete from xshell_config where id=?", id);
        return 1;
    }

    @Override
    public void updateById(XShellConfig xShellConfig) {
        DateTime dateTime = new DateTime();
        String msStr = dateTime.toMsStr();
        jdbcTemplate.update("update xshell_config set jump_ip=?,jump_port=?,jump_pwd=?,target_ip=?,docker_id=?,comment=?,modify_time=?"
                        + " where id=?",
                xShellConfig.getJumpIp(),
                xShellConfig.getJumpPort(),
                xShellConfig.getJumpPwd(),
                xShellConfig.getTargetIp(),
                xShellConfig.getDockerId(),
                xShellConfig.getComment(),
                msStr,
                xShellConfig.getId()
        );
    }
}
