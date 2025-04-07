package org.example.casestudy.controller;

import org.example.casestudy.entities.Authorization;
import org.example.casestudy.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getBasicAuthToken_Success() {
        Authorization auth = new Authorization();
        auth.setUsername("testuser");
        auth.setPassword("testpassword");
        String expectedToken = "generated.jwt.token";

        when(authService.findCustomerByUsername(auth.getUsername(), auth.getPassword())).thenReturn(expectedToken);

        ResponseEntity<String> response = authController.getBasicAuthToken(auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedToken, response.getBody());
        verify(authService, times(1)).findCustomerByUsername(auth.getUsername(), auth.getPassword());
    }

    @Test
    void getBasicAuthToken_AuthServiceThrowsException() {
        Authorization auth = new Authorization();
        auth.setUsername("testuser");
        auth.setPassword("testpassword");
        String errorMessage = "Invalid credentials";

        when(authService.findCustomerByUsername(auth.getUsername(), auth.getPassword())).thenThrow(new IllegalArgumentException(errorMessage));

        try {
            authController.getBasicAuthToken(auth);
        } catch (IllegalArgumentException e) {
            assertEquals(errorMessage, e.getMessage());
        }

        verify(authService, times(1)).findCustomerByUsername(auth.getUsername(), auth.getPassword());
    }
}