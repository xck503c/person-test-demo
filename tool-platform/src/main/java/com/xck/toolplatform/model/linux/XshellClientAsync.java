package com.xck.toolplatform.model.linux;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.xck.toolplatform.util.StringCallable;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class XshellClientAsync {

    private Set<String> stopFlags;
    private XShellConfig xShellConfig;
    private JSch jsch = new JSch();
    private Session session;
    private ChannelShell xShellChannel;
    private PrintWriter printWriter;
    private Thread readRespThread;
    private StringCallable stringCallable;

    private final static String lineSeparator = java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("line.separator"));

    public XshellClientAsync(XShellConfig xShellConfig) {
        this.xShellConfig = xShellConfig;
        this.stopFlags = new HashSet<>();
        stopFlags.add("]$");
        stopFlags.add("]#");
        stopFlags.add("]>");
    }

    public boolean start() {
        try {
            session = jsch.getSession(null, xShellConfig.getJumpIp(), xShellConfig.getJumpPort());
            session.setConfig("StrictHostKeyChecking", "no"); // 不验证 HostKey
            session.setPassword(xShellConfig.getJumpPwd());
            session.connect(xShellConfig.getConnectTimeout());

            xShellChannel = (ChannelShell) session.openChannel("shell");
            xShellChannel.connect(3000);

            OutputStream os = xShellChannel.getOutputStream();
            printWriter = new PrintWriter(os);

            InputStream is = xShellChannel.getInputStream();
            readRespThread = new Thread(() -> {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    char[] buf = new char[1024];
                    while (true) {
                        int len = br.read(buf);
                        if (len == -1) {
                            break;
                        }
                        String resp = new String(buf, 0, len);
                        while (!stringCallable.handle(resp)) {
                            Thread.sleep(3000);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            readRespThread.start();

            sendCommandln(xShellConfig.getTargetIp());

            if (StringUtils.isNotBlank(xShellConfig.getDockerId())) {
                Thread.sleep(1000);
                sendCommandln("sudo docker exec -it " + xShellConfig.getDockerId() + " /bin/bash");
            }

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
                    sendCommand(userInput);
                }
            }
        }
    }

    public void sendCommandln(String command) throws Exception {
        printWriter.println(command);
        printWriter.flush();
    }

    public void sendCommand(String command) throws Exception {
        // 这里不需要println，回车应该由前端控制，然后再进行替换为系统分隔
        boolean isEnter = false;
        while (command.endsWith("\n") || command.endsWith("\r")) {
            command = command.substring(0, command.length()-1);
            isEnter = true;
        }

        if (isEnter) {
            command += lineSeparator;
        }
        printWriter.print(command);
        printWriter.flush();
    }

    public void setStringCallable(StringCallable stringCallable) {
        this.stringCallable = stringCallable;
    }
}
