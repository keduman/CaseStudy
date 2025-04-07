package org.example.casestudy.service;

import jakarta.transaction.Transactional;
import org.example.casestudy.repositories.CustomerRepository;
import org.example.casestudy.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class AuthService {
    private final CustomerRepository customerRepository;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthService(CustomerRepository customerRepository, JwtUtils jwtUtils) {
        this.customerRepository = customerRepository;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    public String findCustomerByUsername(String username, String password) {
        var customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        if (customer.isEnabled() && customer.getPassword().equals(password)) {
            return jwtUtils.generateToken(username);
        } else {
            throw new IllegalArgumentException("Invalid credentials");
        }
    }
}