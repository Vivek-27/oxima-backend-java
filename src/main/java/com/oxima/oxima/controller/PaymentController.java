package com.oxima.oxima.controller;

import com.oxima.oxima.model.Trip;
import com.oxima.oxima.repository.TripRepo;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/payment")
public class PaymentController {


    //DTOs

    // PaymentRequest.java
    public static class PaymentRequest {

        public double getFare() {
            return fare;
        }

        public void setFare(double fare) {
            this.fare = fare;
        }

        public UUID getTripId() {
            return tripId;
        }

        public void setTripId(UUID tripId) {
            this.tripId = tripId;
        }

        private double fare;
        private UUID tripId;
        // getters and setters
    }

    // VerifyPaymentRequest.java
    public static class VerifyPaymentRequest {

        public String getPaymentId() {
            return paymentId;
        }

        public void setPaymentId(String paymentId) {
            this.paymentId = paymentId;
        }

        public UUID getTripId() {
            return tripId;
        }

        public void setTripId(UUID tripId) {
            this.tripId = tripId;
        }

        private String paymentId;
        private UUID tripId;
        // getters and setters
    }


    private final TripRepo tripRepository;

    public PaymentController(TripRepo tripRepository) {
        this.tripRepository = tripRepository;
    }

    @Value("${razorpay.key}")
    private String razorpayKey;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    @PostMapping("/createOrder")
    public ResponseEntity<?> createOrder(@RequestBody PaymentRequest request) {
        try {
            RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);

            int amount = (int) (request.getFare() ); // in paise

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "txn_" + UUID.randomUUID());

            Order order = client.orders.create(orderRequest);

            Optional<Trip> optionalTrip = tripRepository.findById(request.getTripId());
            if (optionalTrip.isEmpty()) return ResponseEntity.badRequest().body("Invalid trip ID");

            Trip trip = optionalTrip.get();
            trip.setFare(amount);
            trip.setPaymentStatus(Trip.PaymentStatus.PENDING);
            tripRepository.save(trip);

            return ResponseEntity.ok(Map.of(
                    "orderId", order.get("id"),
                    "amount", order.get("amount")
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Order creation failed");
        }
    }

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @PatchMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody VerifyPaymentRequest request) {
        Optional<Trip> optionalTrip = tripRepository.findById(request.getTripId());
        if (optionalTrip.isEmpty()) return ResponseEntity.badRequest().body("Invalid trip ID");

        Trip trip = optionalTrip.get();
        trip.setPaymentStatus(Trip.PaymentStatus.PAID);
        trip.setPaymentId(request.getPaymentId());
        tripRepository.save(trip);

        // Notify customer via WebSocket

        messagingTemplate.convertAndSend("/topic/payment/" + trip.getDriverID(),
                trip.getPaymentStatus());
        messagingTemplate.convertAndSend("/topic/payment/" + trip.getCustomerID(),
                trip.getPaymentStatus());

        return ResponseEntity.ok("Payment verified successfully");
    }

    @PatchMapping("/updateStatus/{tripId}")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable String tripId, @RequestBody Map<String, String> body) {
        String status = body.get("status").toUpperCase();
        if (!status.matches("pending|paid|retry")) {
            return ResponseEntity.badRequest().body("Invalid status");
        }

        Optional<Trip> optionalTrip = tripRepository.findById(UUID.fromString(tripId));
        if (optionalTrip.isEmpty()) return ResponseEntity.badRequest().body("Trip not found");

        Trip trip = optionalTrip.get();
        Trip.PaymentStatus newStatus = Trip.PaymentStatus.valueOf(status);
        trip.setPaymentStatus(newStatus);
        tripRepository.save(trip);

        return ResponseEntity.ok("Payment status updated to " + status);
    }
}
