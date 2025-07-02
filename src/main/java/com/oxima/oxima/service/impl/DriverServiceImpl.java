package com.oxima.oxima.service.impl;

import com.oxima.oxima.model.Driver;
import com.oxima.oxima.repository.DriverRepo;
import com.oxima.oxima.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DriverServiceImpl implements DriverService {

    @Autowired
    private DriverRepo driverRepository;

    @Override
    public Driver createDriver(Driver driver) {
        driver.setId(UUID.randomUUID());
        return driverRepository.save(driver);
    }

    @Override
    public Driver getDriverById(UUID id) {
        Optional<Driver> driver = driverRepository.findById(id);
        return driver.orElse(null);
    }

    @Override
    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    @Override
    public Driver updateDriver(UUID id, Driver updatedDriver) {
        if (driverRepository.existsById(id)) {
            updatedDriver.setId(id);
            return driverRepository.save(updatedDriver);
        }
        return null;
    }

    @Override
    public void deleteDriver(UUID id) {
        driverRepository.deleteById(id);
    }
}
