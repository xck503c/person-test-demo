package com.xck.jdk.file.converter;

import cn.hutool.json.JSONObject;
import org.apache.commons.lang.StringUtils;

/**
 * json对象转换
 *
 * @author xuchengkun
 * @date 2021/11/28 00:25
 **/
public class JsonStrConverter extends ObjectConverter {

    public String pwd;

    public JsonStrConverter(String pwd) {
        this.pwd = pwd;
    }

    @Override
    public Object deal(Object input) {
        try {
            String json = new JSONObject(input, false, true).toJSONString(0);
            if (StringUtils.isNotBlank(pwd)) {
                json = EncryptUtil.encodeAES(json, pwd);
            }
            return json;
        } catch (Exception e) {
            throw new ObjectConvertException(e);
        }
    }
}
