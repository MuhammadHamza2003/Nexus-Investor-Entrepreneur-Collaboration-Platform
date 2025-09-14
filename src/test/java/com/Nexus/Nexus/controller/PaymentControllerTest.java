package com.Nexus.Nexus.controller;

import com.Nexus.Nexus.dto.*;
import com.Nexus.Nexus.model.*;
import com.Nexus.Nexus.service.TransactionService;
import com.Nexus.Nexus.service.WalletService;
import com.Nexus.Nexus.service.PaymentMethodService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for PaymentController
 */
@WebMvcTest(PaymentController.class)
@ExtendWith(MockitoExtension.class)
public class PaymentControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private TransactionService transactionService;
    
    @MockBean
    private WalletService walletService;
    
    @MockBean
    private PaymentMethodService paymentMethodService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private User testUser;
    private WalletResponse testWallet;
    private TransactionResponse testTransaction;
    private PaymentMethodResponse testPaymentMethod;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user123");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        
        testWallet = WalletResponse.builder()
            .id("wallet123")
            .balance(new BigDecimal("100.00"))
            .currency("USD")
            .totalDeposited(new BigDecimal("500.00"))
            .totalWithdrawn(new BigDecimal("200.00"))
            .totalTransferred(new BigDecimal("100.00"))
            .totalReceived(new BigDecimal("50.00"))
            .isActive(true)
            .isFrozen(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
            
        testTransaction = TransactionResponse.builder()
            .id("tx123")
            .type(TransactionType.DEPOSIT)
            .amount(new BigDecimal("50.00"))
            .status(TransactionStatus.COMPLETED)
            .description("Test deposit")
            .currency("USD")
            .fee(new BigDecimal("1.75"))
            .netAmount(new BigDecimal("48.25"))
            .createdAt(LocalDateTime.now())
            .completedAt(LocalDateTime.now())
            .build();
            
        testPaymentMethod = PaymentMethodResponse.builder()
            .id("pm123")
            .type(PaymentMethodType.CREDIT_CARD)
            .displayName("Visa ending in 4242")
            .lastFourDigits("4242")
            .brand("visa")
            .formattedExpiration("12/25")
            .isDefault(true)
            .isActive(true)
            .isExpired(false)
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void testGetWallet_Success() throws Exception {
        when(walletService.getWalletResponse(any(User.class))).thenReturn(testWallet);
        
        mockMvc.perform(get("/api/payments/wallet")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("wallet123"))
                .andExpect(jsonPath("$.balance").value(100.00))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.isFrozen").value(false));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void testDeposit_Success() throws Exception {
        DepositRequest request = DepositRequest.builder()
            .amount(new BigDecimal("50.00"))
            .paymentMethodId("pm123")
            .description("Test deposit")
            .build();
            
        when(transactionService.processDeposit(any(User.class), any(DepositRequest.class)))
            .thenReturn(testTransaction);
        
        mockMvc.perform(post("/api/payments/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("tx123"))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void testWithdraw_Success() throws Exception {
        WithdrawRequest request = WithdrawRequest.builder()
            .amount(new BigDecimal("25.00"))
            .paymentMethodId("pm123")
            .description("Test withdrawal")
            .build();
            
        TransactionResponse withdrawResponse = TransactionResponse.builder()
            .id("tx124")
            .type(TransactionType.WITHDRAW)
            .amount(new BigDecimal("25.00"))
            .status(TransactionStatus.COMPLETED)
            .description("Test withdrawal")
            .currency("USD")
            .fee(new BigDecimal("1.03"))
            .netAmount(new BigDecimal("23.97"))
            .createdAt(LocalDateTime.now())
            .completedAt(LocalDateTime.now())
            .build();
            
        when(transactionService.processWithdrawal(any(User.class), any(WithdrawRequest.class)))
            .thenReturn(withdrawResponse);
        
        mockMvc.perform(post("/api/payments/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("WITHDRAW"))
                .andExpect(jsonPath("$.amount").value(25.00))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void testTransfer_Success() throws Exception {
        TransferRequest request = TransferRequest.builder()
            .amount(new BigDecimal("30.00"))
            .recipientUsername("recipient")
            .description("Test transfer")
            .build();
            
        TransactionResponse transferResponse = TransactionResponse.builder()
            .id("tx125")
            .type(TransactionType.TRANSFER)
            .amount(new BigDecimal("30.00"))
            .status(TransactionStatus.COMPLETED)
            .description("Test transfer")
            .recipientUsername("recipient")
            .currency("USD")
            .fee(BigDecimal.ZERO)
            .netAmount(new BigDecimal("30.00"))
            .createdAt(LocalDateTime.now())
            .completedAt(LocalDateTime.now())
            .build();
            
        when(transactionService.processTransfer(any(User.class), any(TransferRequest.class)))
            .thenReturn(transferResponse);
        
        mockMvc.perform(post("/api/payments/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.amount").value(30.00))
                .andExpect(jsonPath("$.recipientUsername").value("recipient"));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void testGetTransactions_Success() throws Exception {
        List<TransactionResponse> transactions = Arrays.asList(testTransaction);
        
        when(transactionService.getTransactionHistory(any(User.class)))
            .thenReturn(transactions);
        
        mockMvc.perform(get("/api/payments/transactions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("tx123"))
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void testGetPaymentMethods_Success() throws Exception {
        List<PaymentMethodResponse> paymentMethods = Arrays.asList(testPaymentMethod);
        
        when(paymentMethodService.getUserPaymentMethods(any(User.class)))
            .thenReturn(paymentMethods);
        
        mockMvc.perform(get("/api/payments/payment-methods")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("pm123"))
                .andExpect(jsonPath("$[0].displayName").value("Visa ending in 4242"));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void testAddPaymentMethod_Success() throws Exception {
        PaymentMethodRequest request = PaymentMethodRequest.builder()
            .type(PaymentMethodType.CREDIT_CARD)
            .paymentToken("pm_test_token")
            .displayName("Test Card")
            .setAsDefault(true)
            .build();
            
        when(paymentMethodService.addPaymentMethod(any(User.class), any(PaymentMethodRequest.class)))
            .thenReturn(testPaymentMethod);
        
        mockMvc.perform(post("/api/payments/payment-methods")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("pm123"))
                .andExpect(jsonPath("$.type").value("CREDIT_CARD"));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void testDeposit_InvalidAmount() throws Exception {
        DepositRequest request = DepositRequest.builder()
            .amount(new BigDecimal("0.50")) // Below minimum
            .paymentMethodId("pm123")
            .description("Invalid deposit")
            .build();
        
        mockMvc.perform(post("/api/payments/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/payments/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Payment System"));
    }
}