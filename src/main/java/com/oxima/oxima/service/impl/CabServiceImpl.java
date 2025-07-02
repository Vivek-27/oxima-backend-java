package com.oxima.oxima.service.impl;

import com.oxima.oxima.model.Cab;
import com.oxima.oxima.repository.CabRepo;
import com.oxima.oxima.service.CabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CabServiceImpl implements CabService {

    @Autowired
    private CabRepo cabRepository;

    @Override
    public Cab createCab(Cab cab) {
        cab.setId(UUID.randomUUID());
        return cabRepository.save(cab);
    }

    @Override
    public Cab getCabById(UUID id) {
        Optional<Cab> cab = cabRepository.findById(id);
        return cab.orElse(null);
    }

    @Override
    public List<Cab> getAllCabs() {
        return cabRepository.findAll();
    }

    @Override
    public Cab updateCab(UUID id, Cab updatedCab) {
        if (cabRepository.existsById(id)) {
            updatedCab.setId(id);
            return cabRepository.save(updatedCab);
        }
        return null;
    }

    @Override
    public void deleteCab(UUID id) {
        cabRepository.deleteById(id);
    }
}
