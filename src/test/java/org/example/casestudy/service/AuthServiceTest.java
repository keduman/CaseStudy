package org.example.casestudy.service;

import org.example.casestudy.entities.Customer;
import org.example.casestudy.repositories.CustomerRepository;
import org.example.casestudy.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findCustomerByUsername_ValidCredentials() {
        String username = "testuser";
        String password = "testpassword";
        Customer customer = new Customer();
        customer.setUsername(username);
        customer.setPassword(password);
        customer.setEnabled(true);

        String expectedToken = "generated.jwt.token";

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));
        when(jwtUtils.generateToken(username)).thenReturn(expectedToken);

        String actualToken = authService.findCustomerByUsername(username, password);

        assertEquals(expectedToken, actualToken);
        verify(customerRepository, times(1)).findByUsername(username);
        verify(jwtUtils, times(1)).generateToken(username);
    }

    @Test
    void findCustomerByUsername_InvalidPassword() {
        String username = "testuser";
        String correctPassword = "correctpassword";
        String incorrectPassword = "wrongpassword";
        Customer customer = new Customer();
        customer.setUsername(username);
        customer.setPassword(correctPassword);
        customer.setEnabled(true);

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.findCustomerByUsername(username, incorrectPassword);
        });

        assertEquals("Invalid credentials", exception.getMessage());
        verify(customerRepository, times(1)).findByUsername(username);
        verify(jwtUtils, never()).generateToken(anyString());
    }

    @Test
    void findCustomerByUsername_UserDisabled() {
        String username = "testuser";
        String password = "testpassword";
        Customer customer = new Customer();
        customer.setUsername(username);
        customer.setPassword(password);
        customer.setEnabled(false);

        when(customerRepository.findByUsername(username)).thenReturn(Optional.of(customer));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.findCustomerByUsername(username, password);
        });

        assertEquals("Invalid credentials", exception.getMessage());
        verify(customerRepository, times(1)).findByUsername(username);
        verify(jwtUtils, never()).generateToken(anyString());
    }

    @Test
    void findCustomerByUsername_UserNotFound() {
        String username = "nonexistentuser";
        String password = "anypassword";

        when(customerRepository.findByUsername(username)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.findCustomerByUsername(username, password);
        });

        assertEquals("Customer not found", exception.getMessage());
        verify(customerRepository, times(1)).findByUsername(username);
        verify(jwtUtils, never()).generateToken(anyString());
    }
}