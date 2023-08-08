package com.xck.toolplatform.controller.tool;

import com.alibaba.fastjson.JSONObject;
import com.xck.toolplatform.model.base.Request;
import com.xck.toolplatform.model.base.Response;
import com.xck.toolplatform.util.GSMCharset;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.nio.charset.Charset;

@Controller
@RequestMapping(path = "/tool/statistics")
public class StatisticsController {

    @RequestMapping(path = {"", "/"}, method = RequestMethod.GET)
    public ModelAndView statistics() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("tool/statistics");
        return modelAndView;
    }

    @RequestMapping(path = "/char", method = RequestMethod.POST)
    @ResponseBody
    public Response statisticsChar(@RequestBody Request request) {
        String requestData = request.getData();
        JSONObject jsonObject = JSONObject.parseObject(requestData);
        String content = jsonObject.getString("content");
        if (content == null) {
            return Response.error(Response.ERR_PARAMS, "请求参数错误");
        }
        return Response.success(String.valueOf(content.length()));
    }

    @RequestMapping(path = "/byte", method = RequestMethod.POST)
    @ResponseBody
    public Response statisticsByte(@RequestBody Request request) {
        String requestData = request.getData();
        JSONObject jsonObject = JSONObject.parseObject(requestData);
        String charsetName = jsonObject.getString("charsetName");
        try {
            String content = jsonObject.getString("content");
            if (content == null) {
                return Response.error(Response.ERR_PARAMS, "请求参数错误");
            }

            byte[] bytes = null;
            if (GSMCharset.charsetName.equals(charsetName)) {
                bytes = GSMCharset.encode(content);
            } else {
                Charset charset = Charset.forName(charsetName);
                bytes = content.getBytes(charset);
            }
            return Response.success(String.valueOf(bytes.length));
        } catch (Exception e) {
            return Response.error(Response.ERR_PARAMS, "不支持编码" + charsetName);
        }
    }
}
