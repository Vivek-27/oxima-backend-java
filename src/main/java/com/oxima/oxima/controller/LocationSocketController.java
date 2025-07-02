package com.oxima.oxima.controller;

import com.google.gson.Gson;
import com.oxima.oxima.model.LocationUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class LocationSocketController {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @MessageMapping("/driver/location")
    public void updateDriverLocation(LocationUpdateRequest update) {
        kafkaTemplate.send("driver-location", new Gson().toJson(update));
    }
}
