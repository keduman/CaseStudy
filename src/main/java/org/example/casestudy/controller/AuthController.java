package org.example.casestudy.controller;

import org.example.casestudy.entities.Authorization;
import org.example.casestudy.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<String> getBasicAuthToken(@RequestBody Authorization authorization) {
        return ResponseEntity.ok(authService.findCustomerByUsername(authorization.getUsername(), authorization.getPassword()));
    }
}
