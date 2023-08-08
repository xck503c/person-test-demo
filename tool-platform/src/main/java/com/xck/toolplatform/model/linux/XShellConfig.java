package com.xck.toolplatform.model.linux;

public class XShellConfig {

    private String jumpUser;
    private String jumpIp;
    private int jumpPort;
    private String jumpPwd;
    private String targetIp;
    private String dockerId;

    /**
     * 注释说明
     */
    private String comment;

    private int connectTimeout = 10000;

    public String getJumpUser() {
        return jumpUser;
    }

    public void setJumpUser(String jumpUser) {
        this.jumpUser = jumpUser;
    }

    public String getJumpIp() {
        return jumpIp;
    }

    public void setJumpIp(String jumpIp) {
        this.jumpIp = jumpIp;
    }

    public int getJumpPort() {
        return jumpPort;
    }

    public void setJumpPort(int jumpPort) {
        this.jumpPort = jumpPort;
    }

    public String getJumpPwd() {
        return jumpPwd;
    }

    public void setJumpPwd(String jumpPwd) {
        this.jumpPwd = jumpPwd;
    }

    public String getTargetIp() {
        return targetIp;
    }

    public void setTargetIp(String targetIp) {
        this.targetIp = targetIp;
    }

    public String getDockerId() {
        return dockerId;
    }

    public void setDockerId(String dockerId) {
        this.dockerId = dockerId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
}
