package com.oxima.oxima.repository;

import com.mongodb.client.model.geojson.Point;
import com.oxima.oxima.model.Customer;
import com.oxima.oxima.model.Driver;
import org.springframework.data.geo.Distance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverRepo extends MongoRepository<Driver, UUID> {

    // Find drivers within 5km of a location
    List<Driver> findByLocationNear(Point location, Distance distance);

    Optional<Driver> findByEmail(String email);

}
