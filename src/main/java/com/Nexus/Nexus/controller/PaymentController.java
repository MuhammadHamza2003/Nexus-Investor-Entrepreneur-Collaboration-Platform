package com.Nexus.Nexus.controller;

import com.Nexus.Nexus.dto.*;
import com.Nexus.Nexus.model.User;
import com.Nexus.Nexus.model.TransactionStatus;
import com.Nexus.Nexus.service.TransactionService;
import com.Nexus.Nexus.service.WalletService;
import com.Nexus.Nexus.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for payment and transaction operations
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {
    
    private final TransactionService transactionService;
    private final WalletService walletService;
    private final PaymentMethodService paymentMethodService;
    
    /**
     * Get user's wallet information
     */
    @GetMapping("/wallet")
    public ResponseEntity<WalletResponse> getWallet(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            WalletResponse wallet = walletService.getWalletResponse(user);
            return ResponseEntity.ok(wallet);
        } catch (Exception e) {
            log.error("Failed to get wallet: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Process a deposit
     */
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@Valid @RequestBody DepositRequest request, 
                                   Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            TransactionResponse transaction = transactionService.processDeposit(user, request);
            
            log.info("Deposit request processed for user: {}", user.getUsername());
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            log.error("Failed to process deposit: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Deposit failed", "message", e.getMessage()));
        }
    }
    
    /**
     * Process a withdrawal
     */
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@Valid @RequestBody WithdrawRequest request, 
                                    Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            TransactionResponse transaction = transactionService.processWithdrawal(user, request);
            
            log.info("Withdrawal request processed for user: {}", user.getUsername());
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            log.error("Failed to process withdrawal: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Withdrawal failed", "message", e.getMessage()));
        }
    }
    
    /**
     * Process a transfer between users
     */
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@Valid @RequestBody TransferRequest request, 
                                    Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            TransactionResponse transaction = transactionService.processTransfer(user, request);
            
            log.info("Transfer request processed for user: {}", user.getUsername());
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            log.error("Failed to process transfer: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Transfer failed", "message", e.getMessage()));
        }
    }
    
    /**
     * Get transaction history
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<TransactionResponse> transactions = transactionService.getTransactionHistory(user);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("Failed to get transactions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get paginated transaction history
     */
    @GetMapping("/transactions/paginated")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Pageable pageable = PageRequest.of(page, size);
            Page<TransactionResponse> transactions = transactionService.getTransactionHistory(user, pageable);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("Failed to get paginated transactions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get transactions by status
     */
    @GetMapping("/transactions/status/{status}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByStatus(
            @PathVariable TransactionStatus status,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<TransactionResponse> transactions = transactionService.getTransactionsByStatus(user, status);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("Failed to get transactions by status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get specific transaction details
     */
    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<?> getTransaction(@PathVariable String transactionId,
                                          Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            TransactionResponse transaction = transactionService.getTransaction(user, transactionId);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            log.error("Failed to get transaction: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Transaction not found", "message", e.getMessage()));
        }
    }
    
    /**
     * Get user's payment methods
     */
    @GetMapping("/payment-methods")
    public ResponseEntity<List<PaymentMethodResponse>> getPaymentMethods(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<PaymentMethodResponse> paymentMethods = paymentMethodService.getUserPaymentMethods(user);
            return ResponseEntity.ok(paymentMethods);
        } catch (Exception e) {
            log.error("Failed to get payment methods: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Add a new payment method
     */
    @PostMapping("/payment-methods")
    public ResponseEntity<?> addPaymentMethod(@Valid @RequestBody PaymentMethodRequest request,
                                            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            PaymentMethodResponse paymentMethod = paymentMethodService.addPaymentMethod(user, request);
            
            log.info("Payment method added for user: {}", user.getUsername());
            return ResponseEntity.ok(paymentMethod);
        } catch (Exception e) {
            log.error("Failed to add payment method: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to add payment method", "message", e.getMessage()));
        }
    }
    
    /**
     * Delete a payment method
     */
    @DeleteMapping("/payment-methods/{paymentMethodId}")
    public ResponseEntity<?> deletePaymentMethod(@PathVariable String paymentMethodId,
                                               Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            paymentMethodService.deletePaymentMethod(user, paymentMethodId);
            
            log.info("Payment method deleted for user: {}", user.getUsername());
            return ResponseEntity.ok(Map.of("message", "Payment method deleted successfully"));
        } catch (Exception e) {
            log.error("Failed to delete payment method: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to delete payment method", "message", e.getMessage()));
        }
    }
    
    /**
     * Set payment method as default
     */
    @PutMapping("/payment-methods/{paymentMethodId}/default")
    public ResponseEntity<?> setDefaultPaymentMethod(@PathVariable String paymentMethodId,
                                                   Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            paymentMethodService.setAsDefault(user, paymentMethodId);
            
            log.info("Default payment method set for user: {}", user.getUsername());
            return ResponseEntity.ok(Map.of("message", "Default payment method set successfully"));
        } catch (Exception e) {
            log.error("Failed to set default payment method: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to set default payment method", "message", e.getMessage()));
        }
    }
    
    /**
     * Health check endpoint for payment system
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Payment System",
            "version", "1.0.0",
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}