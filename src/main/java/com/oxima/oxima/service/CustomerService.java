package com.oxima.oxima.service;

import com.oxima.oxima.model.Customer;
import com.oxima.oxima.model.Location;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

public interface CustomerService {
    Customer createCustomer(Customer customer);

    Customer getCustomerById(UUID id);

    List<Customer> getAllCustomers();

    Customer updateCustomerLocation(UUID id, GeoJsonPoint location);


    Customer updateCustomer(UUID id, Customer updatedCustomer);

    void deleteCustomer(UUID id);
}
