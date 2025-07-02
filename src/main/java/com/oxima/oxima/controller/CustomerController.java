package com.oxima.oxima.controller;

import com.mongodb.DuplicateKeyException;
import com.mongodb.lang.Nullable;
import com.oxima.oxima.config.JavaSmtpGmailSenderService;
import com.oxima.oxima.model.*;
import com.oxima.oxima.repository.CustomerRepo;
import com.oxima.oxima.repository.DriverRepo;
import com.oxima.oxima.repository.OtpRepo;
import com.oxima.oxima.repository.TripRepo;
import com.oxima.oxima.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;


@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    public CustomerController(RedisTemplate<String, String> redisTemplate, SimpMessagingTemplate messagingTemplate) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    // Create new customer
    @PostMapping("/create")
    public ResponseEntity<?> createCustomer(@RequestBody Customer customer) {

        try {
            if ((customer.getEmail() == null || customer.getEmail().isEmpty()) &&
                    (customer.getPhone() == null )) {
                return ResponseEntity.badRequest().body("Please enter email or phone");
            }
            Customer created = customerService.createCustomer(customer);
            return ResponseEntity.ok(created);

        }catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT) // 409
                    .body(e.getMessage());       // Message: "Email or phone already exists"
        }
    }

    // Get customer by ID
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable UUID id) {
        Customer customer = customerService.getCustomerById(id);
        if (customer != null) {
            return ResponseEntity.ok(customer);
        }
        return ResponseEntity.notFound().build();
    }

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private OtpRepo otpRepo;

    @Autowired
    private JavaSmtpGmailSenderService javaSmtpGmailSenderService;

    @GetMapping("/login/{email}")
    public ResponseEntity<?> loginCustomerById(@PathVariable String email) {

        Optional<Customer> customers = customerRepo.findByEmail(email);
        if(customers!=null) {
            Customer customer = customers.get();

            otpRepo.deleteAllByUser(customer.getId());
            String generatedOtp = String.valueOf((int)(Math.random() * 900000) + 100000);
            OTP otp = new OTP();
            otp.setId(UUID.randomUUID());
            otp.setUser(customer.getId());
            otp.setOtp(generatedOtp);
            otp.setOtpExpiry(new Date()); // Now + 5 mins handled by TTL
            otpRepo.save(otp);
            // TODO: Send OTP via email/sms
            javaSmtpGmailSenderService.sendEmail(customer.getEmail(),"Verification code", "Please use the verification code below to sign in.\n" +
                    "\n" +
                    otp.getOtp()+"\n" +
                    "\n" +
                    "If you didn’t request this, you can ignore this email.\n" +
                    "\n" +
                    "Thanks,\n" +
                    "The Oxima team");

            return ResponseEntity.status(HttpStatus.CREATED).body(customer.getId());

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
        Optional<Customer> user = customerRepo.findById(userId);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get()); // send actual user data
        }

        return ResponseEntity.ok("OTP verified successfully!");
    }


    // Get all customers
    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @PatchMapping("/{id}/location")
    public ResponseEntity<Customer> updateLocation(@PathVariable UUID id, @RequestBody GeoJsonPoint location) {
        Customer updatedCustomer = customerService.updateCustomerLocation(id, location);
        if (updatedCustomer != null) {
            return ResponseEntity.ok(updatedCustomer);
        }
        return ResponseEntity.notFound().build();
    }

    // Update customer by ID
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable UUID id, @RequestBody Customer updatedCustomer) {
        Customer updated = customerService.updateCustomer(id, updatedCustomer);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    // Delete customer by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }



    @Autowired
    DriverRepo driverRepo;

    @Autowired
    TripRepo tripRepo;

    @Autowired
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @PostMapping("/requestRide2")
    public ResponseEntity<?> requestRide2(@RequestBody Trip req) {
        System.out.println("Client Trip Request: " + req);
        Optional<Customer> customer = customerRepo.findById(UUID.fromString(req.getCustomerID()));
        if (customer.isEmpty()) return ResponseEntity.badRequest().body("Invalid customer");

        // Use very large radius to get all drivers from Redis GEO
        GeoResults<RedisGeoCommands.GeoLocation<String>> nearby =
                stringRedisTemplate.opsForGeo().radius(
                        "drivers",
                        new Circle(
                                new Point(req.getSource().getLongitude(), req.getSource().getLatitude()),
                                new Distance(50, Metrics.KILOMETERS) // Covers entire Earth
                        ),
                        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().sortAscending()
                );

        if (nearby == null || nearby.getContent().isEmpty())
            return ResponseEntity.status(503).body("No drivers available");

        // Create trip with "PENDING" status and list of driver candidates
        Trip trip = new Trip();
        trip.setId(UUID.randomUUID());
        trip.setCustomerID(req.getCustomerID().toString());
        trip.setStatus(Trip.Status.PENDING);
        trip.setSource(req.getSource());
        trip.setDestination(req.getDestination());
        trip.setCandidateDrivers(new ArrayList<>());

        for (var driverLoc : nearby.getContent()) {
            try {
                UUID driverId = UUID.fromString(driverLoc.getContent().getName());
                trip.getCandidateDrivers().add(driverId.toString());
            } catch (IllegalArgumentException e) {
                // Skip if not a valid UUID
            }
        }

        if (trip.getCandidateDrivers().isEmpty())
            return ResponseEntity.status(503).body("No valid driver IDs found");

        tripRepo.save(trip);



        // Send to first driver
//        return sendToNextDriver(trip, 0);

         ResponseEntity<?> driverResponse = sendToNextDriver(trip, 0);
        if (!driverResponse.getStatusCode().is2xxSuccessful()) {
            return driverResponse;
        }
        return ResponseEntity.ok(Map.of("tripId", trip.getId().toString()));

    }

    private ResponseEntity<?> sendToNextDriver(Trip trip, int index) {
        if (index >= trip.getCandidateDrivers().size()) {
            return ResponseEntity.status(503).body("All drivers denied or unavailable");
        }


        UUID driverId =  UUID.fromString(trip.getCandidateDrivers().get(index));
        Optional<Driver> optionalDriver = driverRepo.findById(driverId);

        if (optionalDriver.isEmpty() || !optionalDriver.get().isAvailable())
            return sendToNextDriver(trip, index + 1);

        // Notify driver via WebSocket
        messagingTemplate.convertAndSend("/topic/ride-assignments/" + driverId,
                trip);


        // Save currentIndex to Redis to track for retries
        stringRedisTemplate.opsForValue().set("trip:" + trip.getId() + ":index", String.valueOf(index));

        return ResponseEntity.ok("Ride offer sent to driver: " + optionalDriver.get().getName());
    }

    @PostMapping("/driver/respond")
    public ResponseEntity<?> driverRespond(@RequestParam UUID tripId, @RequestParam UUID driverId, @RequestParam boolean accepted) {
        Optional<Trip> tripOptional = tripRepo.findById(tripId);
        if (tripOptional.isEmpty()) return ResponseEntity.badRequest().body("Trip not found");

        Trip trip = tripOptional.get();
        String key = "trip:" + trip.getId() + ":index";

        String indexStr = stringRedisTemplate.opsForValue().get(key);
        int currentIndex = indexStr != null ? Integer.parseInt(indexStr) : -1;

        if (!accepted) {
            return sendToNextDriver(trip, currentIndex + 1);
        }

        // Driver accepted

        Optional<Driver> driverOptional = driverRepo.findById(UUID.fromString(String.valueOf(driverId)));
        if (driverOptional.isEmpty()) return ResponseEntity.badRequest().body("Driver not found");

        Driver driver = driverOptional.get();
        if (!driver.isAvailable()) return ResponseEntity.status(409).body("Driver no longer available");

        driver.setAvailable(false);
        driverRepo.save(driver);
        trip.setDriverID(driverId.toString());

        trip.setStatus(Trip.Status.ACCEPTED);
        tripRepo.save(trip);

        // ✅ Store customerId in Redis for live updates
        stringRedisTemplate.opsForValue().set("trip:" + driverId + ":customer", trip.getCustomerID().toString());

        // Notify customer via WebSocket
        messagingTemplate.convertAndSend("/topic/ride-assigned/" + trip.getCustomerID(),
                driver);

        return ResponseEntity.ok("Driver accepted. Trip confirmed."+ driver);
    }




//    @PostMapping("/{tripId}/status")
//    public String updateTripStatus(@PathVariable UUID tripId, @RequestParam Trip.Status status) {
//        Optional<Trip> tripOptional = tripRepo.findById(tripId);
//        if(tripOptional.isEmpty()) return "Trip not found";
//
//        Trip trip = tripOptional.get();
//        trip.setStatus(status);
//        if(status == Trip.Status.COMPLETED) {
//            Driver driver = trip.getDriverID();
//            driver.setAvailable(true);
//            driverRepo.save(driver);
//        }
//        tripRepo.save(trip);
//        return "Trip status updated to: " + status;
//    }



}
