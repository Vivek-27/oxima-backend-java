package com.oxima.oxima.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oxima.oxima.config.JavaSmtpGmailSenderService;
import com.oxima.oxima.model.*;
import com.oxima.oxima.repository.DriverRepo;
import com.oxima.oxima.repository.OtpRepo;
import com.oxima.oxima.repository.TripRepo;
import com.oxima.oxima.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/driver")
@RequiredArgsConstructor
public class DriverController {

    @Autowired
    private DriverService driverService;

    private final KafkaTemplate<Object, String> kafkaTemplate;

    @Autowired
    DriverRepo driverRepo;

    @Autowired
    OtpRepo otpRepo;


    @Autowired
    private JavaSmtpGmailSenderService javaSmtpGmailSenderService;



    @PostMapping("/create")
    public ResponseEntity<?> createDriver(@RequestBody Driver driver) {
        try {
            if ((driver.getEmail() == null || driver.getEmail().isEmpty()) &&
                    (driver== null )) {
                return ResponseEntity.badRequest().body("Please enter email or phone");
            }
            Driver created = driverService.createDriver(driver);
            return ResponseEntity.ok(created);

        }catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT) // 409
                    .body(e.getMessage());       // Message: "Email or phone already exists"
        }
    }


    @GetMapping("/login/{email}")
    public ResponseEntity<?> loginDriverByEmail(@PathVariable String email) {

        Optional<Driver> driverop = driverRepo.findByEmail(email);
        if(driverop!=null) {
            Driver driver = driverop.get();

            otpRepo.deleteAllByUser(driver.getId());
            String generatedOtp = String.valueOf((int)(Math.random() * 900000) + 100000);
            OTP otp = new OTP();
            otp.setId(UUID.randomUUID());
            otp.setUser(driver.getId());
            otp.setOtp(generatedOtp);
            otp.setOtpExpiry(new Date()); // Now + 5 mins handled by TTL
            otpRepo.save(otp);
            // TODO: Send OTP via email/sms
            javaSmtpGmailSenderService.sendEmail(driver.getEmail(),"Verification code", "Please use the verification code below to sign in.\n" +
                    "\n" +
                    otp.getOtp()+"\n" +
                    "\n" +
                    "If you didn’t request this, you can ignore this email.\n" +
                    "\n" +
                    "Thanks,\n" +
                    "The Oxima team");

            return ResponseEntity.status(HttpStatus.CREATED).body(driver.getId());

        }


        return ResponseEntity.notFound().build();
    }


    @PostMapping("/login/verify-otp/{userId}")
    public ResponseEntity<?> verifyOtp(@PathVariable UUID userId, @RequestBody Map<String, String> request) {
        String inputOtp = request.get("inputOtp");
        Optional<OTP> otpOpt = otpRepo.findByUser(userId);
        if (otpOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("OTP not found or already expired");
        }

        OTP otp = otpOpt.get();
        if (!otp.getOtp().trim().equals(inputOtp.trim())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
        }

        otpRepo.delete(otp);
        Optional<Driver> user = driverRepo.findById(userId);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get()); // send actual user data
        }

        return ResponseEntity.ok("OTP verified successfully!");
    }


    @GetMapping("/{id}")
    public ResponseEntity<Driver> getDriverById(@PathVariable UUID id) {
        Driver driver = driverService.getDriverById(id);
        if (driver != null) {
            return ResponseEntity.ok(driver);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Driver>> getAllDrivers() {
        List<Driver> drivers = driverService.getAllDrivers();
        return ResponseEntity.ok(drivers);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Driver> updateDriver(@PathVariable UUID id, @RequestBody Driver updatedDriver) {
        Driver updated = driverService.updateDriver(id, updatedDriver);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable UUID id) {
        driverService.deleteDriver(id);
        return ResponseEntity.noContent().build();
    }

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/updateLocation_")
    public ResponseEntity<String> updateLocation_(@RequestBody LocationUpdateRequest locationUpdateRequest) {
        UUID driverId = locationUpdateRequest.getDriverId();
        double lat = locationUpdateRequest.getLocation().getX();
        double lng = locationUpdateRequest.getLocation().getY();

        System.out.println("Received location update: " + locationUpdateRequest);

        // ✅ Validate coordinates
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            return ResponseEntity.badRequest().body("Invalid coordinates: " + locationUpdateRequest);
        }

        // ✅ Fetch driver from DB
        Optional<Driver> driverOptional = driverRepo.findById(driverId);
        if (driverOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Driver not found");
        }

        Driver driver = driverOptional.get();
        driver.setLocation(new GeoJsonPoint(lng, lat)); // GeoJSON = (lng, lat)
        driver.setAvailable(true);
        driverRepo.save(driver);

        // ✅ Add to Redis GEO if not already present
        List<Point> positions = stringRedisTemplate.opsForGeo().position("drivers", driverId.toString());
        boolean exists = positions != null && !positions.isEmpty() && positions.get(0) != null;

        if (!exists) {
            stringRedisTemplate.opsForGeo().add("drivers", new Point(lng, lat), driverId.toString());
            stringRedisTemplate.opsForSet().add("driver_ids", driverId.toString());
        }

        // ✅ Check if driver is assigned to a customer
        String customerIdStr = stringRedisTemplate.opsForValue().get("trip:" + driverId + ":customer");
        if (customerIdStr != null) {
            messagingTemplate.convertAndSend("/topic/live-location/" + customerIdStr, locationUpdateRequest);
        }

        // ✅ Send to Kafka (convert to KafkaLocation DTO)

        MyGeoLocation myGeoLocation = new MyGeoLocation(driverId, lat, lng);
        // Save to Redis
        redisTemplate.opsForValue().set("driver:" + driverId, myGeoLocation.toString());

        // Persist to DB (Mongo)
        driverRepo.findById(driverId).ifPresent(d -> {
            d.setLocation(new GeoJsonPoint(lng, lat));
            driverRepo.save(d);
        });

        return ResponseEntity.ok("Location updated successfully");
    }
    public static class MyGeoLocation {
        public MyGeoLocation(UUID driverId, double lat, double lng) {
        }

        public UUID getDriverId() {
            return driverId;
        }

        public void setDriverId(UUID driverId) {
            this.driverId = driverId;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        private UUID driverId;
        private double latitude;
        private double longitude;

    }



    @MessageMapping("/driver-location/{customerId}")
    public void sendLocation(@Payload Location location, @DestinationVariable String customerId) {
        messagingTemplate.convertAndSend("/topic/driver-location/" + customerId, location);
    }

    @Autowired
    TripRepo tripRepo;


}
