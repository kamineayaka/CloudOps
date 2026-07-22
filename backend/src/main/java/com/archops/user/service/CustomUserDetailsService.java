package com.archops.user.service;

import com.archops.common.security.AuthUserPrincipal;
import com.archops.user.domain.User;
import com.archops.user.repository.UserRepository;
import java.util.stream.Collectors;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        return new AuthUserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                null,
                user.isEnabled(),
                user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet()));
    }
}
