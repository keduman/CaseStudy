package org.example.casestudy.controller;

import org.example.casestudy.entities.Customer;
import org.example.casestudy.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCustomer_Success() {
        Customer customerToCreate = new Customer();
        customerToCreate.setUsername("testuser");
        customerToCreate.setPassword("password");
        customerToCreate.setRole("ROLE_USER");
        customerToCreate.setEnabled(true);

        Customer createdCustomer = new Customer();
        createdCustomer.setId(java.util.UUID.randomUUID());
        createdCustomer.setUsername(customerToCreate.getUsername());
        createdCustomer.setPassword(customerToCreate.getPassword());
        createdCustomer.setRole(customerToCreate.getRole());
        createdCustomer.setEnabled(customerToCreate.isEnabled());

        when(customerService.createCustomer(customerToCreate)).thenReturn(createdCustomer);

        ResponseEntity<Customer> response = customerController.createCustomer(customerToCreate);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdCustomer.getId(), response.getBody().getId());
        assertEquals(customerToCreate.getUsername(), response.getBody().getUsername());
        assertEquals(customerToCreate.getPassword(), response.getBody().getPassword());
        assertEquals(customerToCreate.getRole(), response.getBody().getRole());
        assertEquals(customerToCreate.isEnabled(), response.getBody().isEnabled());

        verify(customerService, times(1)).createCustomer(customerToCreate);
    }

    @Test
    void getCustomerByUsername_UserFound() {
        String usernameToFind = "existinguser";
        Customer foundCustomer = new Customer();
        foundCustomer.setUsername(usernameToFind);
        foundCustomer.setPassword("encodedPassword");
        foundCustomer.setRole("ROLE_USER");
        foundCustomer.setEnabled(true);

        when(customerService.findByUsername(usernameToFind)).thenReturn(Optional.of(foundCustomer));

        ResponseEntity<Customer> response = customerController.getCustomerByUsername(usernameToFind);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(foundCustomer.getUsername(), response.getBody().getUsername());
        assertEquals(foundCustomer.getPassword(), response.getBody().getPassword());
        assertEquals(foundCustomer.getRole(), response.getBody().getRole());
        assertEquals(foundCustomer.isEnabled(), response.getBody().isEnabled());

        verify(customerService, times(1)).findByUsername(usernameToFind);
    }

    @Test
    void getCustomerByUsername_UserNotFound() {
        String usernameToFind = "nonexistentuser";

        when(customerService.findByUsername(usernameToFind)).thenReturn(Optional.empty());

        ResponseEntity<Customer> response = customerController.getCustomerByUsername(usernameToFind);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(customerService, times(1)).findByUsername(usernameToFind);
    }
}