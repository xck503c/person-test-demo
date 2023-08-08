package com.xck.toolplatform.controller.tool;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.crypto.digest.MD5;
import com.alibaba.fastjson.JSONObject;
import com.xck.toolplatform.model.base.Request;
import com.xck.toolplatform.model.base.Response;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(path = "/tool/coding")
public class CodingController {

    @RequestMapping(path = {"", "/"}, method = RequestMethod.GET)
    public ModelAndView coding() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("tool/coding");
        return modelAndView;
    }

    @RequestMapping(path = "/text", method = RequestMethod.POST)
    @ResponseBody
    public Response doCoding(@RequestBody Request request) {
        String requestData = request.getData();
        JSONObject jsonObject = JSONObject.parseObject(requestData);
        String codingType = jsonObject.getString("codingType");
        String srcContent = jsonObject.getString("content");
        if (srcContent == null) {
            return Response.error(Response.ERR_PARAMS, "请求参数错误");
        }

        String targetContent = srcContent;
        if ("MD5_32".equals(codingType)) {
            targetContent = (new MD5()).digestHex(targetContent);
        } else if ("MD5_16".equals(codingType)) {
            targetContent = (new MD5()).digestHex16(targetContent);
        } else if ("base64".equals(codingType)) {
            targetContent = Base64Encoder.encode(targetContent);
        } else {
            return Response.error(Response.ERR_PARAMS, "请求参数错误");
        }

        return Response.success(targetContent);
    }

    @RequestMapping(path = "/file", method = RequestMethod.POST)
    @ResponseBody
    public Response doCoding(MultipartFile file) {


        return Response.success();
    }
}
