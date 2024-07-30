package com.xck.toolplatform.controller.tool.linux;

import com.alibaba.fastjson.JSONObject;
import com.xck.toolplatform.model.base.Request;
import com.xck.toolplatform.model.base.Response;
import com.xck.toolplatform.model.linux.LinuxServerManager;
import com.xck.toolplatform.model.linux.XShellConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/tool/linux/server/manager")
public class LinuxServerManagerController {

    @Autowired
    private LinuxServerManager linuxServerManager;

    @RequestMapping(path = {"", "/"}, method = RequestMethod.GET)
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("tool/linuxserver/linuxServerManager");
        modelAndView.addObject("data", linuxServerManager.queryAll());
        return modelAndView;
    }

    @RequestMapping(path = {"/addPage"}, method = RequestMethod.GET)
    public ModelAndView addPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("tool/linuxserver/linuxServerManagerDetail");
        return modelAndView;
    }

    @RequestMapping(path = {"/updatePage/{id}"}, method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView updatePage(@PathVariable("id") Long id) {

        XShellConfig xShellConfig = linuxServerManager.query(id);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("tool/linuxserver/linuxServerManagerDetail");
        modelAndView.addObject("data", xShellConfig);
        return modelAndView;
    }

    @RequestMapping(path = {"/addXshellConfig"}, method = RequestMethod.POST)
    @ResponseBody
    public Response addXshellConfig(@RequestBody Request request) {
        String requestData = request.getData();
        JSONObject jsonObject = JSONObject.parseObject(requestData);
        XShellConfig xShellConfig = new XShellConfig();
        xShellConfig.setId(jsonObject.getLong("id"));
        xShellConfig.setJumpIp(jsonObject.getString("jumpIp"));
        xShellConfig.setJumpPort(jsonObject.getIntValue("jumpPort"));
        xShellConfig.setJumpPwd(jsonObject.getString("jumpPassword"));
        xShellConfig.setTargetIp(jsonObject.getString("targetIp"));
        xShellConfig.setDockerId(jsonObject.getString("dockerId"));
        xShellConfig.setComment(jsonObject.getString("comment"));

        if (xShellConfig.getId() != null) {
            linuxServerManager.update(xShellConfig);
            return Response.success("更新成功!!!");
        } else {
            linuxServerManager.create(xShellConfig);
            return Response.success("添加成功!!!");
        }
    }

    @RequestMapping(path = {"/delXshellConfig"}, method = RequestMethod.POST)
    @ResponseBody
    public Response delXshellConfig(@RequestBody Request request) {
        String requestData = request.getData();
        JSONObject jsonObject = JSONObject.parseObject(requestData);
        Long id = jsonObject.getLong("id");
        linuxServerManager.deleteById(id);
        return Response.success("删除成功!!!");
    }

    @RequestMapping(path = {"/queryXshellConfig"}, method = RequestMethod.POST)
    @ResponseBody
    public Response query(@RequestBody Request request) {
        String requestData = request.getData();
        JSONObject jsonObject = JSONObject.parseObject(requestData);
        Long id = jsonObject.getLong("id");
        return Response.success(JSONObject.toJSONString(linuxServerManager.query(id)));
    }

    @RequestMapping(path = "/jumpConsole/{id}", method = RequestMethod.GET)
    public ModelAndView jumpConsole(@PathVariable Long id) {

        XShellConfig xShellConfig = linuxServerManager.query(id);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("tool/linuxserver/linuxConsole");
        modelAndView.addObject("data", xShellConfig);
        return modelAndView;
    }
}
