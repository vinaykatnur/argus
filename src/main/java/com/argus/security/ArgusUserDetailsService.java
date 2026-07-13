package com.argus.security;

import com.argus.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ArgusUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public ArgusUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByEmail(username.toLowerCase())
                .map(ArgusUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
