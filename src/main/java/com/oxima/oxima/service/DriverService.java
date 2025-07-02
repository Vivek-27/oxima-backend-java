package com.oxima.oxima.service;

import com.oxima.oxima.model.Driver;

import java.util.List;
import java.util.UUID;

public interface DriverService {
    Driver createDriver(Driver driver);

    Driver getDriverById(UUID id);

    List<Driver> getAllDrivers();

    Driver updateDriver(UUID id, Driver updatedDriver);

    void deleteDriver(UUID id);
}
