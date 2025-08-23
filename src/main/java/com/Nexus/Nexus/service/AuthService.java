package com.Nexus.Nexus.service;

import com.Nexus.Nexus.dto.*;
import com.Nexus.Nexus.model.User;
import com.Nexus.Nexus.repository.UserRepository;
import com.Nexus.Nexus.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    public AuthResponse register(RegisterRequest registerRequest) {
        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setRole(registerRequest.getRole());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setLocation(registerRequest.getLocation());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        
        return new AuthResponse(null, user.getUsername(), user.getEmail(), user.getRole());
    }
    
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        User user = (User) authentication.getPrincipal();
        
        return new AuthResponse(jwt, user.getUsername(), user.getEmail(), user.getRole());
    }
    
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setEmail(user.getEmail());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setRole(user.getRole());
        userResponse.setBio(user.getBio());
        userResponse.setPortfolio(user.getPortfolio());
        userResponse.setPreferences(user.getPreferences());
        userResponse.setProfilePicture(user.getProfilePicture());
        userResponse.setPhoneNumber(user.getPhoneNumber());
        userResponse.setLocation(user.getLocation());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());
        
        return userResponse;
    }
    
    public UserResponse updateProfile(ProfileUpdateRequest updateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        
        // Find the current user in database
        Optional<User> userOptional = userRepository.findById(user.getId());
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User currentUser = userOptional.get();
        
        // Update fields if provided
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().isEmpty()) {
            // Check if email is already taken by another user
            if (userRepository.existsByEmail(updateRequest.getEmail()) && 
                !currentUser.getEmail().equals(updateRequest.getEmail())) {
                throw new RuntimeException("Error: Email is already in use!");
            }
            currentUser.setEmail(updateRequest.getEmail());
        }
        
        if (updateRequest.getFirstName() != null && !updateRequest.getFirstName().isEmpty()) {
            currentUser.setFirstName(updateRequest.getFirstName());
        }
        
        if (updateRequest.getLastName() != null && !updateRequest.getLastName().isEmpty()) {
            currentUser.setLastName(updateRequest.getLastName());
        }
        
        if (updateRequest.getBio() != null) {
            currentUser.setBio(updateRequest.getBio());
        }
        
        if (updateRequest.getPortfolio() != null) {
            currentUser.setPortfolio(updateRequest.getPortfolio());
        }
        
        if (updateRequest.getPreferences() != null) {
            currentUser.setPreferences(updateRequest.getPreferences());
        }
        
        if (updateRequest.getProfilePicture() != null) {
            currentUser.setProfilePicture(updateRequest.getProfilePicture());
        }
        
        if (updateRequest.getPhoneNumber() != null) {
            currentUser.setPhoneNumber(updateRequest.getPhoneNumber());
        }
        
        if (updateRequest.getLocation() != null) {
            currentUser.setLocation(updateRequest.getLocation());
        }
        
        if (updateRequest.getNewPassword() != null && !updateRequest.getNewPassword().isEmpty()) {
            currentUser.setPassword(passwordEncoder.encode(updateRequest.getNewPassword()));
        }
        
        currentUser.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(currentUser);
        
        // Convert to response
        UserResponse userResponse = new UserResponse();
        userResponse.setId(savedUser.getId());
        userResponse.setUsername(savedUser.getUsername());
        userResponse.setEmail(savedUser.getEmail());
        userResponse.setFirstName(savedUser.getFirstName());
        userResponse.setLastName(savedUser.getLastName());
        userResponse.setRole(savedUser.getRole());
        userResponse.setBio(savedUser.getBio());
        userResponse.setPortfolio(savedUser.getPortfolio());
        userResponse.setPreferences(savedUser.getPreferences());
        userResponse.setProfilePicture(savedUser.getProfilePicture());
        userResponse.setPhoneNumber(savedUser.getPhoneNumber());
        userResponse.setLocation(savedUser.getLocation());
        userResponse.setCreatedAt(savedUser.getCreatedAt());
        userResponse.setUpdatedAt(savedUser.getUpdatedAt());
        
        return userResponse;
    }
}
