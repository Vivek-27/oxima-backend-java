package com.oxima.oxima.service.impl;

import com.mongodb.DuplicateKeyException;
import com.oxima.oxima.model.Customer;
import com.oxima.oxima.model.Location;
import com.oxima.oxima.repository.CustomerRepo;
import com.oxima.oxima.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepo customerRepo;

    @Override
    public Customer createCustomer(Customer customer) {
        customer.setId(UUID.randomUUID());
        try {
            return customerRepo.save(customer);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("Email or phone already exists");
        }
    }

    @Override
    public Customer getCustomerById(UUID id) {
        Optional<Customer> customer = customerRepo.findById(id);
        return customer.orElse(null);
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepo.findAll();
    }

    @Override
    public Customer updateCustomerLocation(UUID id, GeoJsonPoint location) {
        Optional<Customer> optionalCustomer = customerRepo.findById(id);
        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();
            customer.setLocation(location);
            return customerRepo.save(customer);
        }
        return null;
    }


    @Override
    public Customer updateCustomer(UUID id, Customer updatedCustomer) {
        if (customerRepo.existsById(id)) {
            updatedCustomer.setId(id);
            return customerRepo.save(updatedCustomer);
        }
        return null;
    }

    @Override
    public void deleteCustomer(UUID id) {
        customerRepo.deleteById(id);
    }
}
