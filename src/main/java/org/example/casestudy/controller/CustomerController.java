package org.example.casestudy.controller;


import org.example.casestudy.entities.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.example.casestudy.service.CustomerService;

@RestController
@RequestMapping("/api/customers")
@PreAuthorize("hasRole('ADMIN')")
public class CustomerController {

    private CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String role,
            @RequestParam boolean enabled) {
        Customer customer = customerService.createCustomer(username, password, role, enabled);
        return new ResponseEntity<>(customer, HttpStatus.CREATED);
    }

    @GetMapping("/{username}")
    public ResponseEntity<Customer> getCustomerByUsername(@PathVariable String username) {
        return customerService.findByUsername(username)
                .map(customer -> new ResponseEntity<>(customer, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
