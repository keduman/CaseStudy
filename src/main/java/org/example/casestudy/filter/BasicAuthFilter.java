package org.example.casestudy.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.casestudy.entities.Customer;
import org.example.casestudy.repositories.CustomerRepository;
import org.example.casestudy.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Component
public class BasicAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final CustomerService customerService;

    @Autowired
    public BasicAuthFilter(@Lazy CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(AUTHORIZATION_HEADER);

        if (header != null && header.startsWith("Basic ")) {
            String base64Credentials = header.substring(6);
            String credentials = new String(Base64.getDecoder().decode(base64Credentials));
            String[] values = credentials.split(":", 2);
            String username = values[0];
            String password = values[1];

            // Perform your custom validation logic here
            Customer customer = checkUser(username);
            if (username.equals(customer.getUsername()) && password.equals(customer.getPassword())) {
                SecurityContextHolder.getContext().setAuthentication(
                        new PreAuthenticatedAuthenticationToken(
                                customer.getUsername(),
                                null,
                                List.of(new SimpleGrantedAuthority(customer.getRole()))
                        )
                );
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private Customer checkUser(String username) {
        return customerService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    }
}