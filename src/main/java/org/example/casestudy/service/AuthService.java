package org.example.casestudy.service;

import jakarta.transaction.Transactional;
import org.example.casestudy.entities.Customer;
import org.example.casestudy.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class AuthService {
    private final CustomerRepository customerRepository;

    @Autowired
    public AuthService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public String findCustomerByUsername(String username, String password) {
        var customer =  customerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        var token = customer.isEnabled() && customer.getPassword().equals(password) ? createToken(username, password) : null;

        if (token == null) {
            throw new IllegalArgumentException("Invalid credentials");
        } else {
            return token;
        }
    }

    private String createToken(String username, String password) {
        String auth = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
    }
}
