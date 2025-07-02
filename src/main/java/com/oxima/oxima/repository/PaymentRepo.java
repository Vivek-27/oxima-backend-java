package com.oxima.oxima.repository;

import com.oxima.oxima.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface PaymentRepo extends MongoRepository<Payment, UUID> {
}
