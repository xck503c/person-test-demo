package com.xck.other.exception;

/**
 * 异常工厂
 *
 * @author xuchengkun
 * @date 2021/11/16 11:14
 **/
public class ExceptionFactory {

    public static LoginFailException newLoginFailException(String tag, String userId, String ip, int port, int status) {
        StringBuilder sb = new StringBuilder();
        sb.append(tag).append(" userId=").append(userId).append(", srcIp=").append(ip)
                .append(", port=").append(port).append(", status=").append(status);
        return new LoginFailException(sb.toString());
    }
}
