package com.oxima.oxima.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

@Controller
@CrossOrigin("http://localhost:5173")
public class NotificationController {


    @MessageMapping("/sendMessage") // Endpoint matching the JavaScript destination
        @SendTo("/topic/notifications") // Broadcast to subscribers of this topic
    public String sendMessage(String message) {
        System.out.println("Received message: " + message); // Debugging log
        return message; // Broadcast the message
    }

    @MessageMapping("/message")
    @SendTo("/chatroom/public")
    public String receivePublicMessage(String message) {
        return message;
    }

    @Autowired
    private SimpMessagingTemplate template;

    @MessageMapping("/private-message")
    @SendTo("/chat")
    public String sendPrivateMessage( String message) {
        template.convertAndSendToUser("vivek", "/private", message);  // /user/{username}/private
        return message;
    }


}