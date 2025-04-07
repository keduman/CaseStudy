package org.example.casestudy.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.casestudy.entities.Customer;
import org.example.casestudy.service.CustomerService;
import org.example.casestudy.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.io.IOException;
import java.util.Optional;


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class JwtAuthFilterTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testDoFilterInternal_ValidToken() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String username = "testuser";
        String authHeader = "Bearer " + token;
        Customer customer = new Customer();
        customer.setUsername(username);
        customer.setRole("ROLE_USER");

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtils.extractUsername(token)).thenReturn(username);
        when(customerService.findByUsername(username)).thenReturn(Optional.of(customer));
        when(jwtUtils.validateToken(token, username)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Verify that SecurityContextHolder is set with the authentication
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        PreAuthenticatedAuthenticationToken authentication = (PreAuthenticatedAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertEquals(username, authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(customer.getRole())));

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void testDoFilterInternal_InvalidToken() throws ServletException, IOException {
        String token = "invalid.jwt.token";
        String username = "testuser";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtils.extractUsername(token)).thenReturn(username);
        when(customerService.findByUsername(username)).thenReturn(Optional.of(new Customer()));
        when(jwtUtils.validateToken(token, "testuser")).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Verify that SecurityContextHolder is NOT set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void testDoFilterInternal_TokenNotPresent() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Verify that SecurityContextHolder is NOT set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void testDoFilterInternal_TokenDoesNotStartWithBearer() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("InvalidToken");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Verify that SecurityContextHolder is NOT set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void testDoFilterInternal_CustomerNotFound() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String username = "nonexistentuser";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtils.extractUsername(token)).thenReturn(username);
        when(customerService.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(ServletException.class, () -> jwtAuthFilter.doFilterInternal(request, response, filterChain));

        // Verify that SecurityContextHolder is NOT set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(0)).doFilter(request, response);
    }
}