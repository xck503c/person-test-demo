import com.xck.toolplatform.model.linux.XshellClient;
import com.xck.toolplatform.model.linux.XShellConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * 远程登录linux
 *
 * @author xuchengkun
 * @date 2021/12/23 09:27
 **/
public class RemoteLoginLinux {

    private static String mobile = "0630123302_16000_2169";
    private static String msgId = "";

    public static void main(String[] args) throws Exception {

        XShellConfig xShellConfig = new XShellConfig();
        xShellConfig.setJumpIp("192.138.130.38");
        xShellConfig.setJumpPort(12345);
        xShellConfig.setJumpPwd("xckXCK5038334@");
//        xShellConfig.setTargetIp("155");
//        xShellConfig.setDockerId("5dcc6a5f0fde");

        // sms-oapi
//        xShellConfig.setTargetIp("172.17.114.220");
//        xShellConfig.setDockerId("9a04e281642d");

        XshellClient xshellClient = new XshellClient(xShellConfig);
        xshellClient.start();


//        List<String> list = new ArrayList<>();
//        list.add("cd /hskj/local_cluster1/cluster_dealdata/logs");
//        list.add("grep -a '" + mobile + "' info.log");
//        list.add("cd /hskj/sms-splice-charging-service/logs");
//        list.add("grep -a '" + mobile + "' info.log");
//        list.add("cd /hskj/local_cluster1/cluster_receiver/logs");
//        list.add("grep -a '" + mobile + "' info.log");

//        List<String> list = new ArrayList<>();
//        list.add("cd /hskj/sms-splice-charging-service/logs");
//        list.add("grep 'netSmsReqQueue' monitor.log");

        List<String> list = new ArrayList<>();
//        list.add("cd /hskj/sms-oapi/logs");
        list.add("ll");


        execute(xshellClient, list);


        xshellClient.close();
    }

    public static void execute(XshellClient xshellClient, List<String> list) throws Exception{
        for (String command : list) {
            System.out.println(xshellClient.sendCommand(command));
        }
    }

    public static int parseArthasBoot(String arthasBootMsg, String target) throws Exception {
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
