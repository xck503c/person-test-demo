package com.xck.jdk.file.config;

public class Message {

    private long sn;

    private String user_id;

    private String msg_id;

    private String complete_content;

    public long getSn() {
        return sn;
    }

    public void setSn(long sn) {
        this.sn = sn;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(String msg_id) {
        this.msg_id = msg_id;
    }

    public String getComplete_content() {
        return complete_content;
    }

    public void setComplete_content(String complete_content) {
        this.complete_content = complete_content;
    }
}
