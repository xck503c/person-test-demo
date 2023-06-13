package com.xck.toolplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ToolPlatFormMain {

    public static void main(String[] args) {
        try {
            SpringApplication.run(ToolPlatFormMain.class);
            Thread.sleep(1000000L);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
