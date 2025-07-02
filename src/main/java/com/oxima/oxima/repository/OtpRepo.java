package com.oxima.oxima.repository;

import com.oxima.oxima.model.OTP;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OtpRepo extends MongoRepository<OTP, UUID> {
    List<OTP> findAllByUser(UUID user);
    Optional<OTP> findByUser(UUID user);
    Optional<OTP> findTopByUserOrderByOtpExpiryDesc(UUID user); // only the latest
    void deleteAllByUser(UUID user);


}
