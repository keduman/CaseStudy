package org.example.casestudy.service;

import org.example.casestudy.entities.Customer;
import org.example.casestudy.repositories.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCustomer_SuccessfulCreation() {
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

        when(customerRepository.save(customerToCreate)).thenReturn(createdCustomer);

        Customer actualCustomer = customerService.createCustomer(customerToCreate);

        assertEquals(createdCustomer.getId(), actualCustomer.getId());
        assertEquals(customerToCreate.getUsername(), actualCustomer.getUsername());
        assertEquals(customerToCreate.getPassword(), actualCustomer.getPassword());
        assertEquals(customerToCreate.getRole(), actualCustomer.getRole());
        assertEquals(customerToCreate.isEnabled(), actualCustomer.isEnabled());

        verify(customerRepository, times(1)).save(customerToCreate);
    }

    @Test
    void findByUsername_UserFound() {
        String usernameToFind = "existinguser";
        Customer foundCustomer = new Customer();
        foundCustomer.setUsername(usernameToFind);
        foundCustomer.setPassword("encodedPassword");
        foundCustomer.setRole("ROLE_USER");
        foundCustomer.setEnabled(true);

        when(customerRepository.findByUsername(usernameToFind)).thenReturn(Optional.of(foundCustomer));

        Optional<Customer> actualCustomer = customerService.findByUsername(usernameToFind);

        assertTrue(actualCustomer.isPresent());
        assertEquals(foundCustomer.getUsername(), actualCustomer.get().getUsername());
        assertEquals(foundCustomer.getPassword(), actualCustomer.get().getPassword());
        assertEquals(foundCustomer.getRole(), actualCustomer.get().getRole());
        assertEquals(foundCustomer.isEnabled(), actualCustomer.get().isEnabled());

        verify(customerRepository, times(1)).findByUsername(usernameToFind);
    }

    @Test
    void findByUsername_UserNotFound() {
        String usernameToFind = "nonexistentuser";

        when(customerRepository.findByUsername(usernameToFind)).thenReturn(Optional.empty());

        Optional<Customer> actualCustomer = customerService.findByUsername(usernameToFind);

        assertTrue(actualCustomer.isEmpty());

        verify(customerRepository, times(1)).findByUsername(usernameToFind);
    }
}