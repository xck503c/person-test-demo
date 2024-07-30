package com.xck.toolplatform.controller.tool.linux;

import com.alibaba.fastjson.JSONObject;
import com.xck.toolplatform.model.base.Request;
import com.xck.toolplatform.model.base.Response;
import com.xck.toolplatform.model.linux.XShellConfig;
import com.xck.toolplatform.model.linux.XshellClientAsync;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * 每次连接建立成功都会创建一个新对象
 */
@Component
@ServerEndpoint("/tool/linux/server/console/ws")
public class LinuxConsoleWebSocketServer {

    private final static String COMMAND_TYPE_LOGIN = "login";
    private final static String COMMAND_TYPE_COMMAND = "command";
    private final static String COMMAND_TYPE_OUT = "out";

    private XshellClientAsync xshellClient;

    private Session session;

    /**
     * 连接建立成功回调
     *
     * @param session
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    /**
     * 连接关闭回调
     */
    @OnClose
    public void onClose() {
        try {
            session.close();
        } catch (IOException e) {
        }
    }

    /**
     * 收到客户端消息回调
     * @param message
     * @param session
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            Request request = JSONObject.parseObject(message, Request.class);
            String type = request.getType();
            switch (type) {
                case COMMAND_TYPE_LOGIN:
                    if (xshellClient != null) {
                        sendMessage(JSONObject.toJSONString(Response.error(Response.ERR_OTHER, "reapeat login")));
                        return;
                    }
                    String requestData = request.getData();
                    JSONObject jsonObject = JSONObject.parseObject(requestData);

                    XShellConfig xShellConfig = new XShellConfig();
                    xShellConfig.setJumpIp(jsonObject.getString("jumpIp"));
                    xShellConfig.setJumpPort(jsonObject.getIntValue("jumpPort"));
                    xShellConfig.setJumpPwd(jsonObject.getString("jumpPassword"));
                    xShellConfig.setTargetIp(jsonObject.getString("targetIp"));
                    xShellConfig.setDockerId(jsonObject.getString("dockerId"));
                    xshellClient = new XshellClientAsync(xShellConfig);
                    xshellClient.setStringCallable(this::sendMessage);
                    if (xshellClient.start()) {
                        sendMessage("login success");
                    } else {
                        sendMessage("login fail");
                        xshellClient = null;
                    }
                    break;
                case COMMAND_TYPE_COMMAND:
                    JSONObject dataObj = JSONObject.parseObject(request.getData());
                    xshellClient.sendCommand(dataObj.getString("command"));
                    break;
                case COMMAND_TYPE_OUT:
                    xshellClient.close();
                    xshellClient = null;
                    onClose();
                    break;
                default:
                    System.out.println("warn");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(JSONObject.toJSONString(Response.error(Response.ERR_OTHER, "error")));
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    public boolean sendMessage(String message) {
        try {
            this.session.getBasicRemote().sendText(JSONObject.toJSONString(Response.success(message)));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
