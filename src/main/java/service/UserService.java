package service;

import entities.Customer;
import entities.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import repositories.CustomerRepository;
import repositories.UserRoleRepository;

import java.util.List;

@Service
public class UserService implements UserDetailsService {
    private final CustomerRepository customerRepository;
    private final UserRoleRepository userRoleRepository;

    @Autowired
    public UserService(CustomerRepository customerRepository, UserRoleRepository userRoleRepository) {
        this.customerRepository = customerRepository;
        this.userRoleRepository = userRoleRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found"));

        List<UserRole> userRoles = userRoleRepository.findByCustomerId(customer.getId());

        var authorities = userRoles.stream()
                .map(userRole -> new SimpleGrantedAuthority(userRole.getRole().getName()))
                .toList();

        return new org.springframework.security.core.userdetails.User(
                customer.getUsername(),
                customer.getPassword(),
                authorities
        );
    }
}
