package com.xck.toolplatform.model.base;

public class Response {

    public final static int SUCCESS = 200;
    public final static int ERR_PARAMS = 201;

    public final static int ERR_OTHER = 202;

    private String data;

    private int code;

    private String desc;

    public static Response success() {
        Response response = new Response();
        response.setCode(SUCCESS);
        response.setDesc("成功");
        return response;
    }

    public static Response success(String data) {
        Response response = new Response();
        response.setCode(SUCCESS);
        response.setDesc("成功");
        response.setData(data);
        return response;
    }

    public static Response error(int code, String desc) {
        Response response = new Response();
        response.setCode(code);
        response.setDesc(desc);
        return response;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
