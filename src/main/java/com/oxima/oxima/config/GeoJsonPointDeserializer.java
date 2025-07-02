package com.oxima.oxima.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.io.IOException;

public class GeoJsonPointDeserializer extends JsonDeserializer<GeoJsonPoint> {

    @Override
    public GeoJsonPoint deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);

        JsonNode coordinatesNode = node.get("coordinates");
        if (coordinatesNode == null || !coordinatesNode.isArray() || coordinatesNode.size() != 2) {
            throw new IOException("Invalid GeoJSON format for GeoJsonPoint. Expected {\"type\":\"Point\",\"coordinates\":[lng, lat]}");
        }

        double lng = coordinatesNode.get(0).asDouble();
        double lat = coordinatesNode.get(1).asDouble();

        return new GeoJsonPoint(lng, lat);
    }
}
