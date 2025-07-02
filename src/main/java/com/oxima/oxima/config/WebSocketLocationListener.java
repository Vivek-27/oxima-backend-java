package com.oxima.oxima.config;


import com.google.gson.Gson;
import com.oxima.oxima.model.LocationUpdateRequest;
import com.oxima.oxima.repository.DriverRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketLocationListener {

    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketLocationListener(RedisTemplate<String, String> redisTemplate,
                                     SimpMessagingTemplate messagingTemplate) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    @Autowired
    DriverRepo driverRepo;

    @KafkaListener(topics = "driver-location", groupId = "location-group")
    public void listen(String message) {
        try {
            System.out.println("Received Kafka Message: " + message);

            LocationUpdateRequest update = new Gson().fromJson(message, LocationUpdateRequest.class);

            // Save to Redis
            redisTemplate.opsForValue().set("driver:" + update.getDriverId(), message);

            // Send to WebSocket subscribers
            messagingTemplate.convertAndSend("/topic/location/" + update.getDriverId(), message);

            // Optionally: persist to MongoDB (update driver's location)
            driverRepo.findById(update.getDriverId()).ifPresent(driver -> {
                driver.setLocation(update.getLocation());
                driverRepo.save(driver);
            });
        } catch (Exception e) {
            System.err.println("Error while processing Kafka message: " + message);
            e.printStackTrace(); // So you can see the actual cause
        }
    }
}
