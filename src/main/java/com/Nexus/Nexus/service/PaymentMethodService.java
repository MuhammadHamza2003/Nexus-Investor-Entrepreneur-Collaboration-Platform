package com.Nexus.Nexus.service;

import com.Nexus.Nexus.dto.*;
import com.Nexus.Nexus.model.*;
import com.Nexus.Nexus.repository.PaymentMethodRepository;
import com.stripe.model.PaymentMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing user payment methods
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentMethodService {
    
    private final PaymentMethodRepository paymentMethodRepository;
    private final StripeService stripeService;
    
    /**
     * Add a new payment method for user
     */
    @Transactional
    public PaymentMethodResponse addPaymentMethod(User user, PaymentMethodRequest request) {
        try {
            // Get payment method details from Stripe
            PaymentMethod stripePaymentMethod = stripeService.getPaymentMethod(request.getPaymentToken());
            
            // Create customer in Stripe if needed (this would typically be done during user registration)
            String customerId = "cus_mock_" + user.getId(); // In real implementation, store this in User entity
            
            // Attach payment method to customer
            stripeService.attachPaymentMethod(request.getPaymentToken(), customerId);
            
            // If this should be default and user has other payment methods, update them
            if (request.isSetAsDefault()) {
                removeDefaultFromOtherMethods(user);
            }
            
            // Create our payment method entity
            com.Nexus.Nexus.model.PaymentMethod paymentMethod = com.Nexus.Nexus.model.PaymentMethod.builder()
                .user(user)
                .type(request.getType())
                .displayName(request.getDisplayName() != null ? request.getDisplayName() : generateDisplayName(stripePaymentMethod))
                .externalId(request.getPaymentToken())
                .isDefault(request.isSetAsDefault())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
                
            // Set card-specific fields if it's a card
            if ("card".equals(stripePaymentMethod.getType())) {
                // In real implementation, you'd extract these from stripePaymentMethod.getCard()
                paymentMethod.setLastFourDigits("4242"); // Mock data
                paymentMethod.setBrand("visa");
                paymentMethod.setExpirationMonth(12);
                paymentMethod.setExpirationYear(2025);
            }
            
            paymentMethod = paymentMethodRepository.save(paymentMethod);
            log.info("Added payment method for user: {}", user.getUsername());
            
            return toResponse(paymentMethod);
            
        } catch (Exception e) {
            log.error("Failed to add payment method for user {}: {}", user.getUsername(), e.getMessage(), e);
            throw new RuntimeException("Failed to add payment method", e);
        }
    }
    
    /**
     * Get all payment methods for user
     */
    public List<PaymentMethodResponse> getUserPaymentMethods(User user) {
        List<com.Nexus.Nexus.model.PaymentMethod> paymentMethods = 
            paymentMethodRepository.findByUserAndIsActiveTrue(user);
            
        return paymentMethods.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get default payment method for user
     */
    public Optional<com.Nexus.Nexus.model.PaymentMethod> getDefaultPaymentMethod(User user) {
        return paymentMethodRepository.findByUserAndIsDefaultTrueAndIsActiveTrue(user);
    }
    
    /**
     * Delete a payment method
     */
    @Transactional
    public void deletePaymentMethod(User user, String paymentMethodId) {
        Optional<com.Nexus.Nexus.model.PaymentMethod> paymentMethod = 
            paymentMethodRepository.findById(paymentMethodId);
            
        if (paymentMethod.isEmpty() || !paymentMethod.get().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Payment method not found or not owned by user");
        }
        
        com.Nexus.Nexus.model.PaymentMethod pm = paymentMethod.get();
        pm.setActive(false);
        pm.setUpdatedAt(LocalDateTime.now());
        
        paymentMethodRepository.save(pm);
        log.info("Deleted payment method {} for user: {}", paymentMethodId, user.getUsername());
    }
    
    /**
     * Set payment method as default
     */
    @Transactional
    public void setAsDefault(User user, String paymentMethodId) {
        Optional<com.Nexus.Nexus.model.PaymentMethod> paymentMethod = 
            paymentMethodRepository.findById(paymentMethodId);
            
        if (paymentMethod.isEmpty() || !paymentMethod.get().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Payment method not found or not owned by user");
        }
        
        // Remove default from other methods
        removeDefaultFromOtherMethods(user);
        
        // Set this one as default
        com.Nexus.Nexus.model.PaymentMethod pm = paymentMethod.get();
        pm.setDefault(true);
        pm.setUpdatedAt(LocalDateTime.now());
        
        paymentMethodRepository.save(pm);
        log.info("Set payment method {} as default for user: {}", paymentMethodId, user.getUsername());
    }
    
    /**
     * Find payment method by ID and verify ownership
     */
    public com.Nexus.Nexus.model.PaymentMethod getPaymentMethodForUser(User user, String paymentMethodId) {
        Optional<com.Nexus.Nexus.model.PaymentMethod> paymentMethod = 
            paymentMethodRepository.findById(paymentMethodId);
            
        if (paymentMethod.isEmpty() || !paymentMethod.get().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Payment method not found or not owned by user");
        }
        
        return paymentMethod.get();
    }
    
    /**
     * Remove default flag from all other payment methods for user
     */
    private void removeDefaultFromOtherMethods(User user) {
        List<com.Nexus.Nexus.model.PaymentMethod> defaultMethods = 
            paymentMethodRepository.findByUserAndIsActiveTrue(user)
                .stream()
                .filter(pm -> pm.isDefault())
                .collect(Collectors.toList());
                
        for (com.Nexus.Nexus.model.PaymentMethod pm : defaultMethods) {
            pm.setDefault(false);
            pm.setUpdatedAt(LocalDateTime.now());
            paymentMethodRepository.save(pm);
        }
    }
    
    /**
     * Convert entity to response DTO
     */
    private PaymentMethodResponse toResponse(com.Nexus.Nexus.model.PaymentMethod paymentMethod) {
        return PaymentMethodResponse.builder()
            .id(paymentMethod.getId())
            .type(paymentMethod.getType())
            .displayName(paymentMethod.getDisplayName())
            .lastFourDigits(paymentMethod.getLastFourDigits())
            .brand(paymentMethod.getBrand())
            .formattedExpiration(paymentMethod.getFormattedExpiration())
            .isDefault(paymentMethod.isDefault())
            .isActive(paymentMethod.isActive())
            .isExpired(paymentMethod.isExpired())
            .createdAt(paymentMethod.getCreatedAt())
            .build();
    }
    
    /**
     * Generate display name for payment method
     */
    private String generateDisplayName(PaymentMethod stripePaymentMethod) {
        if ("card".equals(stripePaymentMethod.getType())) {
            // In real implementation, extract from stripePaymentMethod.getCard()
            return "Visa ending in 4242"; // Mock
        }
        return "Payment Method";
    }
}