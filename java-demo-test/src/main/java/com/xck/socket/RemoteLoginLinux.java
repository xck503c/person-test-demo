package com.xck.socket;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.*;

/**
 * 远程登录linux
 *
 * @author xuchengkun
 * @date 2021/12/23 09:27
 **/
public class RemoteLoginLinux {

    private static String keyPath = "D:\\dependency\\pangchuanpu.pem";

    public static void main(String[] args) throws Exception {
        JSch jsch = new JSch();
        jsch.addIdentity(keyPath, "KSKWiL9ZXHGr7tdz");
        Session session = jsch.getSession("pangchuanpu", "192.168.6.49", 22);
        session.setConfig("StrictHostKeyChecking", "no"); // 不验证 HostKey
        session.connect(30000);

        Channel channel = session.openChannel("shell");
        channel.connect(3000);

        readAndWait(channel, "Opt or ID>:");

        OutputStream os = channel.getOutputStream();
        os.write("172.17.114.222\n".getBytes());
        os.flush();

        readAndWait(channel, "]$");

        os.write("sudo docker exec -it 0be3b1105f5c /bin/bash\n".getBytes());
        os.flush();

        readAndWait(channel, "]#");

        os.write("cd /hskj/\n".getBytes());
        os.flush();

        readAndWait(channel, "]#");

        os.write("java -jar arthas-boot.jar\n".getBytes());
        os.flush();

        String arthasBootMsg = readAndWait(channel, "MainReceiver");
        int order = parseArthasBoot(arthasBootMsg, "MainReceiver");

        os.write((order + "\n").getBytes());
        os.flush();

        readAndWait(channel, "]$");

//        channel.disconnect();
//        session.disconnect();
    }

    public static String readAndWait(Channel channel, String stopFlag) throws Exception{
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(channel.getInputStream(), "UTF-8"));
        char[] buf = new char[1024];
        while (true) {
            int len = br.read(buf);
            String readMsg = new String(buf, 0, len);
            sb.append(readMsg);
            if (readMsg.contains(stopFlag)) {
                System.out.println(sb.toString());
                return sb.toString();
            }
        }
    }

    public static int parseArthasBoot(String arthasBootMsg, String target) throws Exception{
       String[] s = arthasBootMsg.split("\\n");
       for (String line : s) {
           if (line.contains(target)) {
               int start = line.indexOf("[");
               int end = line.indexOf("]");
               return Integer.parseInt(line.substring(start + 1, end));
           }
       }
       return -1;
    }
}
