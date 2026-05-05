package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String value) throws UsernameNotFoundException {
        return userRepository.findByEmail(value)
                .or(() -> userRepository.findByUsername(value))
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

//    @Override
//    public UserDetails loadUserByUsername(String value) throws UsernameNotFoundException {
//        Users user = userRepository.findByEmail(value)
//                .or(() -> userRepository.findByUsername(value))
//                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
//
//        return new org.springframework.security.core.userdetails.User(
//                user.getUsername(),           // username field (can be email if you prefer)
//                user.getPassword(),           // hashed password
//                user.getRoles().stream()
//                        .map(role -> new SimpleGrantedAuthority(role.getName()))
//                        .toList()                 // list of authorities
//        );
//    }
}
