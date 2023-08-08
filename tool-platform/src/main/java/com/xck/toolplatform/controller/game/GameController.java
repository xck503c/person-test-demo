package com.xck.toolplatform.controller.game;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/game")
public class GameController {

    @RequestMapping(path = "/greedySnake", method = RequestMethod.GET)
    public ModelAndView greedySnake() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("game/greedySnake");
        return modelAndView;
    }
}
