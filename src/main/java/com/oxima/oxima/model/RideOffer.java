package com.oxima.oxima.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideOffer {
    private UUID tripId;
    private Location source;
    private Location destination;
}
