package org.example.casestudy.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.casestudy.entities.Customer;
import org.example.casestudy.service.CustomerService;
import org.example.casestudy.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final CustomerService customerService;
    private final JwtUtils jwtUtils;

    @Autowired
    public JwtAuthFilter(@Lazy CustomerService customerService, JwtUtils jwtUtils) {
        this.customerService = customerService;
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(AUTHORIZATION_HEADER);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            String username = jwtUtils.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Customer customer = customerService.findByUsername(username)
                        .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

                if (jwtUtils.validateToken(token, customer.getUsername())) {
                    SecurityContextHolder.getContext().setAuthentication(
                            new PreAuthenticatedAuthenticationToken(
                                    customer.getUsername(),
                                    null,
                                    List.of(new SimpleGrantedAuthority(customer.getRole()))
                            )
                    );
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}