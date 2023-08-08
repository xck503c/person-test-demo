package com.xck.toolplatform.controller.common;

import com.alibaba.fastjson.JSONObject;
import com.xck.toolplatform.config.UrlConfig;
import com.xck.toolplatform.model.base.Response;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class DefaultController {

    @RequestMapping(path = {"", "/"}, method = RequestMethod.GET)
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index");
        modelAndView.addObject("functionList", UrlConfig.getDefaultUrls());
        return modelAndView;
    }

    @RequestMapping(value = "/urls", method = RequestMethod.POST)
    @ResponseBody
    public Response urls() {
        return Response.success(JSONObject.toJSONString(UrlConfig.getDefaultUrls()));
    }
}
