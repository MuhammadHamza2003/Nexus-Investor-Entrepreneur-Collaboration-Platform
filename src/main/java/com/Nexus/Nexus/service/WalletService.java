package com.Nexus.Nexus.service;

import com.Nexus.Nexus.dto.*;
import com.Nexus.Nexus.model.*;
import com.Nexus.Nexus.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for managing user wallets and balances
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {
    
    private final WalletRepository walletRepository;
    
    /**
     * Get or create wallet for user
     */
    @Transactional
    public Wallet getOrCreateWallet(User user) {
        Optional<Wallet> existingWallet = walletRepository.findByUser(user);
        
        if (existingWallet.isPresent()) {
            return existingWallet.get();
        }
        
        // Create new wallet
        Wallet wallet = Wallet.builder()
            .user(user)
            .balance(BigDecimal.ZERO)
            .currency("USD")
            .totalDeposited(BigDecimal.ZERO)
            .totalWithdrawn(BigDecimal.ZERO)
            .totalTransferred(BigDecimal.ZERO)
            .totalReceived(BigDecimal.ZERO)
            .isActive(true)
            .isFrozen(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
            
        wallet = walletRepository.save(wallet);
        log.info("Created new wallet for user: {}", user.getUsername());
        return wallet;
    }
    
    /**
     * Get wallet response DTO for user
     */
    public WalletResponse getWalletResponse(User user) {
        Wallet wallet = getOrCreateWallet(user);
        
        return WalletResponse.builder()
            .id(wallet.getId())
            .balance(wallet.getBalance())
            .currency(wallet.getCurrency())
            .totalDeposited(wallet.getTotalDeposited())
            .totalWithdrawn(wallet.getTotalWithdrawn())
            .totalTransferred(wallet.getTotalTransferred())
            .totalReceived(wallet.getTotalReceived())
            .isActive(wallet.isActive())
            .isFrozen(wallet.isFrozen())
            .createdAt(wallet.getCreatedAt())
            .updatedAt(wallet.getUpdatedAt())
            .build();
    }
    
    /**
     * Add funds to wallet (for deposits)
     */
    @Transactional
    public void addFunds(User user, BigDecimal amount) {
        Wallet wallet = getOrCreateWallet(user);
        
        if (!wallet.isActive() || wallet.isFrozen()) {
            throw new RuntimeException("Wallet is not available for transactions");
        }
        
        wallet.addToBalance(amount);
        wallet.setTotalDeposited(wallet.getTotalDeposited().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        
        walletRepository.save(wallet);
        log.info("Added {} to wallet for user: {}", amount, user.getUsername());
    }
    
    /**
     * Subtract funds from wallet (for withdrawals and transfers)
     */
    @Transactional
    public boolean subtractFunds(User user, BigDecimal amount) {
        Wallet wallet = getOrCreateWallet(user);
        
        if (!wallet.hasSufficientBalance(amount)) {
            log.warn("Insufficient balance for user: {}. Required: {}, Available: {}", 
                user.getUsername(), amount, wallet.getBalance());
            return false;
        }
        
        wallet.subtractFromBalance(amount);
        wallet.setUpdatedAt(LocalDateTime.now());
        
        walletRepository.save(wallet);
        log.info("Subtracted {} from wallet for user: {}", amount, user.getUsername());
        return true;
    }
    
    /**
     * Record withdrawal in wallet stats
     */
    @Transactional
    public void recordWithdrawal(User user, BigDecimal amount) {
        Wallet wallet = getOrCreateWallet(user);
        wallet.setTotalWithdrawn(wallet.getTotalWithdrawn().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);
    }
    
    /**
     * Record transfer out in wallet stats
     */
    @Transactional
    public void recordTransferOut(User user, BigDecimal amount) {
        Wallet wallet = getOrCreateWallet(user);
        wallet.setTotalTransferred(wallet.getTotalTransferred().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);
    }
    
    /**
     * Record transfer in in wallet stats
     */
    @Transactional
    public void recordTransferIn(User user, BigDecimal amount) {
        Wallet wallet = getOrCreateWallet(user);
        wallet.addToBalance(amount);
        wallet.setTotalReceived(wallet.getTotalReceived().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);
    }
    
    /**
     * Check if user has sufficient balance
     */
    public boolean hasSufficientBalance(User user, BigDecimal amount) {
        Wallet wallet = getOrCreateWallet(user);
        return wallet.hasSufficientBalance(amount);
    }
    
    /**
     * Freeze wallet (prevent transactions)
     */
    @Transactional
    public void freezeWallet(User user) {
        Wallet wallet = getOrCreateWallet(user);
        wallet.setFrozen(true);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);
        log.info("Froze wallet for user: {}", user.getUsername());
    }
    
    /**
     * Unfreeze wallet
     */
    @Transactional
    public void unfreezeWallet(User user) {
        Wallet wallet = getOrCreateWallet(user);
        wallet.setFrozen(false);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);
        log.info("Unfroze wallet for user: {}", user.getUsername());
    }
}