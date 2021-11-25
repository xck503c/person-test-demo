package com.xck.other.exception;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 登录异常测试
 *
 * @author xuchengkun
 * @date 2021/11/16 10:57
 **/
public class LoginExceptionTest {

    public static Log log = LogFactory.getLog("info");

    public static void main(String[] args) {

        try {
            int status = 1;
            if(status != 0){
                throw ExceptionFactory.newLoginFailException("http", "aaa", "127.0.0.1", 8888, status);
            }
        } catch (BusinessException e) {
            log.error(e.getMessage());
            if (e instanceof AutoCloseConnection) {
                System.out.println("close");
            }
        }
    }
}
