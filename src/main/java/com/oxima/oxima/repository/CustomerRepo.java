package com.oxima.oxima.repository;

import com.oxima.oxima.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepo extends MongoRepository<Customer, UUID> {
    Customer findById(Customer customerID);
    Optional<Customer> findByEmail(String email);

}
