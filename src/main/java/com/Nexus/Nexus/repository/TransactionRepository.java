package com.Nexus.Nexus.repository;

import com.Nexus.Nexus.model.Transaction;
import com.Nexus.Nexus.model.TransactionStatus;
import com.Nexus.Nexus.model.TransactionType;
import com.Nexus.Nexus.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Transaction entity operations
 */
@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {
    
    /**
     * Find all transactions for a specific user
     */
    List<Transaction> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * Find all transactions for a specific user with pagination
     */
    Page<Transaction> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    /**
     * Find transactions by user and status
     */
    List<Transaction> findByUserAndStatusOrderByCreatedAtDesc(User user, TransactionStatus status);
    
    /**
     * Find transactions by user and type
     */
    List<Transaction> findByUserAndTypeOrderByCreatedAtDesc(User user, TransactionType type);
    
    /**
     * Find transactions by user, status and type
     */
    List<Transaction> findByUserAndStatusAndTypeOrderByCreatedAtDesc(
        User user, TransactionStatus status, TransactionType type);
    
    /**
     * Find transactions by user within date range
     */
    @Query("{'user': ?0, 'createdAt': {'$gte': ?1, '$lte': ?2}}")
    List<Transaction> findByUserAndDateRange(User user, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find transactions where user is recipient (for transfers)
     */
    List<Transaction> findByRecipientUserOrderByCreatedAtDesc(User recipientUser);
    
    /**
     * Find transaction by external transaction ID
     */
    Optional<Transaction> findByExternalTransactionId(String externalTransactionId);
    
    /**
     * Find all pending transactions for a user
     */
    @Query("{'user': ?0, 'status': {'$in': ['PENDING', 'PROCESSING']}}")
    List<Transaction> findPendingTransactionsByUser(User user);
    
    /**
     * Count completed transactions for a user
     */
    long countByUserAndStatus(User user, TransactionStatus status);
    
    /**
     * Find recent transactions (last N transactions)
     */
    List<Transaction> findTop10ByUserOrderByCreatedAtDesc(User user);
    
    /**
     * Find all transactions involving a user (as sender or recipient)
     */
    @Query("{'$or': [{'user': ?0}, {'recipientUser': ?0}]}")
    List<Transaction> findAllTransactionsInvolvingUser(User user);
    
    /**
     * Find transactions by multiple statuses
     */
    List<Transaction> findByUserAndStatusInOrderByCreatedAtDesc(User user, List<TransactionStatus> statuses);
}