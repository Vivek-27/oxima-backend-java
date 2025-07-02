package com.oxima.oxima.repository;

import com.oxima.oxima.model.Cab;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface CabRepo extends MongoRepository<Cab, UUID> {

}
