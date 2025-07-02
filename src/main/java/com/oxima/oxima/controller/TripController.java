package com.oxima.oxima.controller;


import com.oxima.oxima.model.Trip;
import com.oxima.oxima.repository.TripRepo;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/trip")
public class TripController {

    @Autowired
    TripRepo tripRepo;


    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PatchMapping("/cancelTrip/{tripId}/{id}")
    public ResponseEntity<?> cancleTrip(@PathVariable UUID tripId, @PathVariable String id) {


        Optional<Trip> optionalTrip = tripRepo.findById(tripId);
        if(optionalTrip.isEmpty()) {
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trip not found");
        }

        Trip trip = optionalTrip.get();

        if(!trip.getCustomerID().toString().equals(id) && !trip.getDriverID().toString().equals(id)) {
//            System.out.println("UserId: "+ id + " CustomerId: "+ trip.getCustomerID()+" DriverId: "+trip.getDriverID());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to cancel this trip.");
        }

        //updating status
        trip.setStatus(Trip.Status.CANCELED);
        tripRepo.save(trip);

        // Notify customer via WebSocket
        messagingTemplate.convertAndSend("/topic/messages/" + trip.getCustomerID(),
                "Trip Canceled");
        messagingTemplate.convertAndSend("/topic/messages/" + trip.getDriverID(),
                "Trip Canceled");

        return  ResponseEntity.noContent().build();

    }

    @PatchMapping("/completeTrip/{tripId}/{id}")
    public ResponseEntity<?> completeTrip(@PathVariable UUID tripId, @PathVariable String id) {

        Optional<Trip> optionalTrip = tripRepo.findById(tripId);
        if (optionalTrip.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trip not found");
        }

        Trip trip = optionalTrip.get();

        // Only driver should be allowed to complete the trip
        if (!trip.getDriverID().toString().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the assigned driver can complete this trip.");
        }

        // Check if already canceled or completed
        if (trip.getStatus() == Trip.Status.CANCELED || trip.getStatus() == Trip.Status.COMPLETED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Trip already finished or canceled.");
        }

        // Mark trip as completed
        trip.setStatus(Trip.Status.COMPLETED);
        tripRepo.save(trip);

        // Notify both customer and driver via WebSocket
        messagingTemplate.convertAndSend("/topic/messages/" + trip.getCustomerID(), "Trip Completed");
        messagingTemplate.convertAndSend("/topic/messages/" + trip.getDriverID(), "Trip Completed");

        return ResponseEntity.ok("Trip marked as completed");
    }

    @GetMapping("/getAllRides/{id}")
    public ResponseEntity<?> getAllRides(@PathVariable String id) {
        List<Trip> rides = tripRepo.findByDriverIDOrCustomerID(id, id);

        if (rides.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(rides);
    }


    public static class FareRequest {
        public double getDistance() {
            return distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }

        private double distance;
        // add time if needed
    }

    public static class FareResponse {
        private double fare;

        public FareResponse(double fare) {
            this.fare = fare;
        }

        public double getFare() {
            return fare;
        }
    }



    @PostMapping("/calculateFare")
    public ResponseEntity<FareResponse> calculateFare(@RequestBody FareRequest request) {
        double baseFare = 30;
        double perKmRate = 17;
        double distance = request.getDistance(); // in km

        double estimatedFare = baseFare + (distance * perKmRate);

        FareResponse response = new FareResponse(estimatedFare);
        return ResponseEntity.ok(response);
    }


    @PatchMapping("/trip/{tripId}/payment")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable UUID tripId, @RequestParam Trip.PaymentStatus status) {
        Trip trip = tripRepo.findById(tripId).orElseThrow();
        trip.setPaymentStatus(status);
        tripRepo.save(trip);
        return ResponseEntity.ok("Payment status updated");
    }

}
