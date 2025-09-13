package com.Nexus.Nexus.repository;

import com.Nexus.Nexus.model.Wallet;
import com.Nexus.Nexus.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Wallet entity operations
 */
@Repository
public interface WalletRepository extends MongoRepository<Wallet, String> {
    
    /**
     * Find wallet by user
     */
    Optional<Wallet> findByUser(User user);
    
    /**
     * Find active wallet by user
     */
    Optional<Wallet> findByUserAndIsActiveTrue(User user);
    
    /**
     * Check if wallet exists for user
     */
    boolean existsByUser(User user);
    
    /**
     * Find all active wallets
     */
    Iterable<Wallet> findByIsActiveTrue();
}