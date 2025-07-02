package com.oxima.oxima.model;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.oxima.oxima.config.GeoJsonPointDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateRequest {
    private UUID driverId;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    @JsonDeserialize(using = GeoJsonPointDeserializer.class)
    private GeoJsonPoint location;
}
