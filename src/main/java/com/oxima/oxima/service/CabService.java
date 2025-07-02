package com.oxima.oxima.service;

import com.oxima.oxima.model.Cab;

import java.util.List;
import java.util.UUID;

public interface CabService {
    Cab createCab(Cab cab);

    Cab getCabById(UUID id);

    List<Cab> getAllCabs();

    Cab updateCab(UUID id, Cab updatedCab);

    void deleteCab(UUID id);
}
