package com.Nexus.Nexus.service;

import com.Nexus.Nexus.dto.*;
import com.Nexus.Nexus.model.*;
import com.Nexus.Nexus.repository.TransactionRepository;
import com.Nexus.Nexus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing financial transactions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final PaymentMethodService paymentMethodService;
    private final StripeService stripeService;
    
    @Value("${payment.transaction-fee-percentage:0.029}")
    private double feePercentage;
    
    @Value("${payment.fixed-fee:0.30}")
    private double fixedFee;
    
    /**
     * Process a deposit transaction
     */
    @Transactional
    public TransactionResponse processDeposit(User user, DepositRequest request) {
        try {
            // Validate payment method
            PaymentMethod paymentMethod = null;
            if (request.getPaymentMethodId() != null) {
                paymentMethod = paymentMethodService.getPaymentMethodForUser(user, request.getPaymentMethodId());
            }
            
            // Calculate fees
            BigDecimal fee = calculateFee(request.getAmount());
            BigDecimal netAmount = request.getAmount().subtract(fee);
            
            // Create transaction record
            Transaction transaction = Transaction.builder()
                .user(user)
                .type(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .status(TransactionStatus.PENDING)
                .description(request.getDescription() != null ? request.getDescription() : "Wallet deposit")
                .paymentMethod(paymentMethod != null ? paymentMethod.getType() : PaymentMethodType.STRIPE)
                .fee(fee)
                .netAmount(netAmount)
                .currency("USD")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            
            transaction = transactionRepository.save(transaction);
            
            // Process payment with Stripe
            String paymentIntentId;
            if (request.getPaymentToken() != null) {
                // One-time payment
                paymentIntentId = stripeService.createPaymentIntent(
                    request.getAmount(), 
                    "cus_mock_" + user.getId(), 
                    request.getPaymentToken()
                );
            } else if (paymentMethod != null) {
                // Using saved payment method
                paymentIntentId = stripeService.createPaymentIntent(
                    request.getAmount(), 
                    "cus_mock_" + user.getId(), 
                    paymentMethod.getExternalId()
                );
            } else {
                throw new RuntimeException("No payment method provided");
            }
            
            transaction.setExternalTransactionId(paymentIntentId);
            
            // Check payment status (in real implementation, this would be handled by webhooks)
            if (stripeService.isPaymentIntentSucceeded(paymentIntentId)) {
                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setCompletedAt(LocalDateTime.now());
                
                // Add funds to wallet
                walletService.addFunds(user, netAmount);
                
                log.info("Deposit completed for user {}: ${}", user.getUsername(), netAmount);
            } else {
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setErrorMessage("Payment failed");
                log.warn("Deposit failed for user {}: ${}", user.getUsername(), request.getAmount());
            }
            
            transaction.setUpdatedAt(LocalDateTime.now());
            transaction = transactionRepository.save(transaction);
            
            return toResponse(transaction);
            
        } catch (Exception e) {
            log.error("Failed to process deposit for user {}: {}", user.getUsername(), e.getMessage(), e);
            
            // Create failed transaction record
            Transaction failedTransaction = Transaction.builder()
                .user(user)
                .type(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .status(TransactionStatus.FAILED)
                .errorMessage(e.getMessage())
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
                
            failedTransaction = transactionRepository.save(failedTransaction);
            return toResponse(failedTransaction);
        }
    }
    
    /**
     * Process a withdrawal transaction
     */
    @Transactional
    public TransactionResponse processWithdrawal(User user, WithdrawRequest request) {
        try {
            // Validate payment method
            PaymentMethod paymentMethod = paymentMethodService.getPaymentMethodForUser(user, request.getPaymentMethodId());
            
            // Check sufficient balance
            if (!walletService.hasSufficientBalance(user, request.getAmount())) {
                throw new RuntimeException("Insufficient balance");
            }
            
            // Calculate fees
            BigDecimal fee = calculateFee(request.getAmount());
            BigDecimal netAmount = request.getAmount().subtract(fee);
            
            // Create transaction record
            Transaction transaction = Transaction.builder()
                .user(user)
                .type(TransactionType.WITHDRAW)
                .amount(request.getAmount())
                .status(TransactionStatus.PENDING)
                .description(request.getDescription() != null ? request.getDescription() : "Wallet withdrawal")
                .paymentMethod(paymentMethod.getType())
                .fee(fee)
                .netAmount(netAmount)
                .currency("USD")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            
            transaction = transactionRepository.save(transaction);
            
            // Subtract funds from wallet immediately (will be reversed if withdrawal fails)
            if (!walletService.subtractFunds(user, request.getAmount())) {
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setErrorMessage("Insufficient balance");
                transaction.setUpdatedAt(LocalDateTime.now());
                transactionRepository.save(transaction);
                throw new RuntimeException("Insufficient balance");
            }
            
            // In real implementation, process withdrawal with payment gateway
            // For now, we'll simulate success (in production, this would be async via webhooks)
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());
            transaction.setExternalTransactionId("wd_mock_" + System.currentTimeMillis());
            
            // Record withdrawal stats
            walletService.recordWithdrawal(user, request.getAmount());
            
            transaction.setUpdatedAt(LocalDateTime.now());
            transaction = transactionRepository.save(transaction);
            
            log.info("Withdrawal completed for user {}: ${}", user.getUsername(), request.getAmount());
            return toResponse(transaction);
            
        } catch (Exception e) {
            log.error("Failed to process withdrawal for user {}: {}", user.getUsername(), e.getMessage(), e);
            
            // Create failed transaction record
            Transaction failedTransaction = Transaction.builder()
                .user(user)
                .type(TransactionType.WITHDRAW)
                .amount(request.getAmount())
                .status(TransactionStatus.FAILED)
                .errorMessage(e.getMessage())
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
                
            failedTransaction = transactionRepository.save(failedTransaction);
            return toResponse(failedTransaction);
        }
    }
    
    /**
     * Process a transfer transaction between users
     */
    @Transactional
    public TransactionResponse processTransfer(User sender, TransferRequest request) {
        try {
            // Find recipient user
            Optional<User> recipientOpt = userRepository.findByUsername(request.getRecipientUsername());
            if (recipientOpt.isEmpty()) {
                throw new RuntimeException("Recipient user not found");
            }
            User recipient = recipientOpt.get();
            
            // Can't transfer to self
            if (sender.getId().equals(recipient.getId())) {
                throw new RuntimeException("Cannot transfer to yourself");
            }
            
            // Check sufficient balance
            if (!walletService.hasSufficientBalance(sender, request.getAmount())) {
                throw new RuntimeException("Insufficient balance");
            }
            
            // Calculate fees (transfers might have different fee structure)
            BigDecimal fee = BigDecimal.ZERO; // No fee for transfers in this implementation
            BigDecimal netAmount = request.getAmount().subtract(fee);
            
            // Create transaction record
            Transaction transaction = Transaction.builder()
                .user(sender)
                .recipientUser(recipient)
                .type(TransactionType.TRANSFER)
                .amount(request.getAmount())
                .status(TransactionStatus.PENDING)
                .description(request.getDescription() != null ? request.getDescription() : 
                    "Transfer to " + recipient.getUsername())
                .fee(fee)
                .netAmount(netAmount)
                .currency("USD")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            
            transaction = transactionRepository.save(transaction);
            
            // Process transfer
            if (walletService.subtractFunds(sender, request.getAmount())) {
                walletService.recordTransferOut(sender, request.getAmount());
                walletService.recordTransferIn(recipient, netAmount);
                
                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setCompletedAt(LocalDateTime.now());
                
                log.info("Transfer completed from {} to {}: ${}", 
                    sender.getUsername(), recipient.getUsername(), request.getAmount());
            } else {
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setErrorMessage("Insufficient balance");
            }
            
            transaction.setUpdatedAt(LocalDateTime.now());
            transaction = transactionRepository.save(transaction);
            
            return toResponse(transaction);
            
        } catch (Exception e) {
            log.error("Failed to process transfer for user {}: {}", sender.getUsername(), e.getMessage(), e);
            
            // Create failed transaction record
            Transaction failedTransaction = Transaction.builder()
                .user(sender)
                .type(TransactionType.TRANSFER)
                .amount(request.getAmount())
                .status(TransactionStatus.FAILED)
                .errorMessage(e.getMessage())
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
                
            failedTransaction = transactionRepository.save(failedTransaction);
            return toResponse(failedTransaction);
        }
    }
    
    /**
     * Get transaction history for user
     */
    public List<TransactionResponse> getTransactionHistory(User user) {
        List<Transaction> transactions = transactionRepository.findByUserOrderByCreatedAtDesc(user);
        return transactions.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get paginated transaction history
     */
    public Page<TransactionResponse> getTransactionHistory(User user, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return transactions.map(this::toResponse);
    }
    
    /**
     * Get transactions by status
     */
    public List<TransactionResponse> getTransactionsByStatus(User user, TransactionStatus status) {
        List<Transaction> transactions = transactionRepository.findByUserAndStatusOrderByCreatedAtDesc(user, status);
        return transactions.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get transaction by ID (if user owns it)
     */
    public TransactionResponse getTransaction(User user, String transactionId) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
        
        if (transactionOpt.isEmpty()) {
            throw new RuntimeException("Transaction not found");
        }
        
        Transaction transaction = transactionOpt.get();
        
        // Check if user owns this transaction (either as sender or recipient)
        if (!transaction.getUser().getId().equals(user.getId()) && 
            (transaction.getRecipientUser() == null || 
             !transaction.getRecipientUser().getId().equals(user.getId()))) {
            throw new RuntimeException("Transaction not found");
        }
        
        return toResponse(transaction);
    }
    
    /**
     * Calculate transaction fee
     */
    private BigDecimal calculateFee(BigDecimal amount) {
        BigDecimal percentageFee = amount.multiply(new BigDecimal(feePercentage));
        BigDecimal totalFee = percentageFee.add(new BigDecimal(fixedFee));
        return totalFee.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Convert transaction entity to response DTO
     */
    private TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
            .id(transaction.getId())
            .type(transaction.getType())
            .amount(transaction.getAmount())
            .status(transaction.getStatus())
            .description(transaction.getDescription())
            .recipientUsername(transaction.getRecipientUser() != null ? 
                transaction.getRecipientUser().getUsername() : null)
            .paymentMethod(transaction.getPaymentMethod())
            .currency(transaction.getCurrency())
            .fee(transaction.getFee())
            .netAmount(transaction.getNetAmount())
            .errorMessage(transaction.getErrorMessage())
            .createdAt(transaction.getCreatedAt())
            .completedAt(transaction.getCompletedAt())
            .build();
    }
}