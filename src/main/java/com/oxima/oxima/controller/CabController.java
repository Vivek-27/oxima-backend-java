package com.oxima.oxima.controller;

import com.oxima.oxima.model.Cab;
import com.oxima.oxima.service.CabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cab")
public class CabController {

    @Autowired
    private CabService cabService;

    @PostMapping
    public ResponseEntity<Cab> createCab(@RequestBody Cab cab) {
        Cab created = cabService.createCab(cab);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cab> getCabById(@PathVariable UUID id) {
        Cab cab = cabService.getCabById(id);
        if (cab != null) {
            return ResponseEntity.ok(cab);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Cab>> getAllCabs() {
        List<Cab> cabs = cabService.getAllCabs();
        return ResponseEntity.ok(cabs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cab> updateCab(@PathVariable UUID id, @RequestBody Cab updatedCab) {
        Cab updated = cabService.updateCab(id, updatedCab);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCab(@PathVariable UUID id) {
        cabService.deleteCab(id);
        return ResponseEntity.noContent().build();
    }
}
