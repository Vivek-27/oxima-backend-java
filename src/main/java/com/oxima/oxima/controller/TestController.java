package com.oxima.oxima.controller;


import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
@Controller
public class TestController {

    @MessageMapping("/hello")  // client sends to /app/hello
    @SendTo("/topic/greetings") // client subscribes to /topic/greetings
    public String handle(String message) {
        System.out.println("📥 Received from client: " + message);
        return "👋 Hello, " + message;
    }


}
