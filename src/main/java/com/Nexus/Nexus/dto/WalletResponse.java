package com.Nexus.Nexus.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for wallet response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletResponse {
    
    private String id;
    private BigDecimal balance;
    private String currency;
    private BigDecimal totalDeposited;
    private BigDecimal totalWithdrawn;
    private BigDecimal totalTransferred;
    private BigDecimal totalReceived;
    private boolean isActive;
    private boolean isFrozen;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}