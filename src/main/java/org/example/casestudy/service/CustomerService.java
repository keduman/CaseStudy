package org.example.casestudy.service;

import jakarta.transaction.Transactional;
import org.example.casestudy.entities.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.example.casestudy.repositories.CustomerRepository;

import java.util.Optional;

@Service
@Transactional
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public Optional<Customer> findByUsername(String username) {
        return customerRepository.findByUsername(username);
    }
}
