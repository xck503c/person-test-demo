package com.xck.toolplatform.model.linux;

import com.jcraft.jsch.*;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class XshellClient {

    private Set<String> stopFlags;
    private XShellConfig xShellConfig;
    private JSch jsch = new JSch();
    private Session session;
    private ChannelShell xShellChannel;
    private PrintWriter printWriter;

    public XshellClient(XShellConfig xShellConfig) {
        this.xShellConfig = xShellConfig;
        this.stopFlags = new HashSet<>();
        stopFlags.add("]$");
        stopFlags.add("]#");
    }

    public boolean start() {
        try {
            session = jsch.getSession(xShellConfig.getJumpUser(), xShellConfig.getJumpIp(), xShellConfig.getJumpPort());
            session.setConfig("StrictHostKeyChecking", "no"); // 不验证 HostKey
            session.setPassword(xShellConfig.getJumpPwd());
            session.connect(xShellConfig.getConnectTimeout());

            xShellChannel = (ChannelShell) session.openChannel("shell");
            xShellChannel.connect(3000);

            readResponse(">");

            OutputStream os = xShellChannel.getOutputStream();
            printWriter = new PrintWriter(os);

            sendCommand(xShellConfig.getTargetIp());

            if (StringUtils.isNotBlank(xShellConfig.getDockerId())) {
                sendCommand("sudo docker exec -it " + xShellConfig.getDockerId() + " /bin/bash");
            }

            System.out.println("登录成功!!!");

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            close();
        }

        return false;
    }

    public void close() {
        if (xShellChannel != null) {
            xShellChannel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
    }

    public void startTerminal() throws Exception{

        while (true) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String userInput = reader.readLine();
                if ("exit".equals(userInput)) {
                    break;
                } else {
                    System.out.println(sendCommand(userInput));;
                }
            }
        }
    }

    public String sendCommand(String command, String... others) throws Exception {
        printWriter.println(command);
        printWriter.flush();

        return readResponse(others);
    }

    private String readResponse(String... others) throws Exception {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(xShellChannel.getInputStream(), "UTF-8"));
        char[] buf = new char[1024];
        while (true) {
            int len = br.read(buf);
            String readMsg = new String(buf, 0, len);
            sb.append(readMsg);
            for (String stopFlag : stopFlags) {
                if (readMsg.contains(stopFlag)) {
                    return sb.toString();
                }
            }

            if (others != null) {
                for (String stopFlag : others) {
                    if (readMsg.contains(stopFlag)) {
                        return sb.toString();
                    }
                }
            }
        }
    }
}
