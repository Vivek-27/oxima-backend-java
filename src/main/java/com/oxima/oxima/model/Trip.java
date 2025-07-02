package com.oxima.oxima.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Document(collection = "Trips")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Trip {

    @Id
    private UUID id;

    private String customerID;
    private String driverID;
    private Status status;
    private Location source;
    private Location destination;
    private List<String> candidateDrivers = new ArrayList<>();

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private double fare;

    // ✅ Only keep this one — Mongo will store enum as String automatically
    private PaymentStatus paymentStatus;

    private String paymentId;

    public enum Status {
        REQUESTED,
        ACCEPTED,
        CANCELED,
        COMPLETED,
        PENDING
    }

    public enum PaymentStatus {
        PENDING,
        PAID,
        RETRY
    }
}
