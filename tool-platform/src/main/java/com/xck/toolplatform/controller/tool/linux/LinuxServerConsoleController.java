package com.xck.toolplatform.controller.tool.linux;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONObject;
import com.xck.toolplatform.model.base.Request;
import com.xck.toolplatform.model.base.Response;
import com.xck.toolplatform.model.linux.XShellConfig;
import com.xck.toolplatform.model.linux.XshellClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Deprecated
@Controller
@RequestMapping("/tool/linux/server/console")
public class LinuxServerConsoleController {

    static XshellClient xshellClient;
    @RequestMapping(path = {"", "/"}, method = RequestMethod.GET)
    public ModelAndView linuxIndex() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("tool/linuxserver/linuxConsole");
        return modelAndView;
    }

    @RequestMapping(path = {"/newSession"}, method = RequestMethod.POST)
    @ResponseBody
    public Response newSession(@RequestBody Request request) {
        String requestData = request.getData();
        JSONObject jsonObject = JSONObject.parseObject(requestData);

        XShellConfig xShellConfig = new XShellConfig();
        xShellConfig.setJumpIp(jsonObject.getString("jumpIp"));
        xShellConfig.setJumpPort(jsonObject.getIntValue("jumpPort"));
        xShellConfig.setJumpPwd(jsonObject.getString("jumpPassword"));
        xShellConfig.setTargetIp(jsonObject.getString("targetIp"));
        xShellConfig.setDockerId(jsonObject.getString("dockerId"));
        XshellClient xshellClient = new XshellClient(xShellConfig);
        if (xshellClient.start()) {
            LinuxServerConsoleController.xshellClient = xshellClient;
            return Response.success("登录成功!!!");
        } else {
            return Response.success("登录失敗!!!");
        }
    }

    @RequestMapping(path = {"/outSession"}, method = RequestMethod.POST)
    @ResponseBody
    public Response closeSession(@RequestBody Request request) {
        String requestData = request.getData();

        if (LinuxServerConsoleController.xshellClient != null) {
            LinuxServerConsoleController.xshellClient.close();
            LinuxServerConsoleController.xshellClient = null;
            return Response.success("退出成功!!!");
        }

        return Response.success("重复退出!!!");
    }

    @RequestMapping(path = {"/command"}, method = RequestMethod.POST)
    @ResponseBody
    public Response linuxCommand(@RequestBody Request request) {

        if (xshellClient == null) {
            return Response.error(Response.ERR_OTHER, "未登录");
        }

        String requestData = request.getData();
        JSONObject jsonObject = JSONObject.parseObject(requestData);

        String command = jsonObject.getString("command");
        String fileUrl = jsonObject.getString("fileUrl");
        try {
            String resp = xshellClient.sendCommand(command);
            if (StringUtils.isNotBlank(fileUrl)) {
                FileUtil.writeUtf8String(resp, fileUrl);
            }
            System.out.println(resp);
            return Response.success(resp);
        } catch (Exception e) {
            return Response.error(Response.ERR_OTHER, "请求异常, msg=" + e.getMessage());
        }
    }
}
