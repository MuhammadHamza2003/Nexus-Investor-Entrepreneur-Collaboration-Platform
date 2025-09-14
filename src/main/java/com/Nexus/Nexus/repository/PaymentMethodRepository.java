package com.Nexus.Nexus.repository;

import com.Nexus.Nexus.model.PaymentMethod;
import com.Nexus.Nexus.model.PaymentMethodType;
import com.Nexus.Nexus.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PaymentMethod entity operations
 */
@Repository
public interface PaymentMethodRepository extends MongoRepository<PaymentMethod, String> {
    
    /**
     * Find all payment methods for a specific user
     */
    List<PaymentMethod> findByUserAndIsActiveTrue(User user);
    
    /**
     * Find all payment methods for a user (including inactive)
     */
    List<PaymentMethod> findByUser(User user);
    
    /**
     * Find payment methods by user and type
     */
    List<PaymentMethod> findByUserAndTypeAndIsActiveTrue(User user, PaymentMethodType type);
    
    /**
     * Find default payment method for a user
     */
    Optional<PaymentMethod> findByUserAndIsDefaultTrueAndIsActiveTrue(User user);
    
    /**
     * Find payment method by external ID
     */
    Optional<PaymentMethod> findByExternalId(String externalId);
    
    /**
     * Count active payment methods for a user
     */
    long countByUserAndIsActiveTrue(User user);
    
    /**
     * Check if user has any payment methods
     */
    boolean existsByUserAndIsActiveTrue(User user);
}