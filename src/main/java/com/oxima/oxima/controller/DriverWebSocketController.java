package com.oxima.oxima.controller;


import com.oxima.oxima.model.LocationUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class DriverWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/driver-location")
    public void handleDriverLocation(LocationUpdateRequest request) {
        System.out.println("WebSocket received: " + request);
        messagingTemplate.convertAndSend("/topic/driver-location", request);
    }
}
