package com.oxima.oxima.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OTP {

    @Id
    private UUID id;
    @Indexed(unique = true)
    private UUID user;
    private String otp;
    @Indexed(expireAfterSeconds = 300) // 5 minutes
    private Date otpExpiry;
}
