package com.oxima.oxima.repository;

import com.oxima.oxima.model.Trip;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface TripRepo extends MongoRepository<Trip, UUID> {

    List<Trip> findByDriverIDOrCustomerID(String driverId, String customerId);

}
