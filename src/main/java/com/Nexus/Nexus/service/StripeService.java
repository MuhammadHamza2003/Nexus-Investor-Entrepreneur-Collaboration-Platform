package com.Nexus.Nexus.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Customer;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.CustomerCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for integrating with Stripe payment gateway
 * This is configured for sandbox/test mode
 */
@Service
@Slf4j
public class StripeService {
    
    @Value("${payment.stripe.api-key:sk_test_mock_key}")
    private String stripeApiKey;
    
    @Value("${payment.default-currency:USD}")
    private String defaultCurrency;
    
    @PostConstruct
    public void init() {
        if (stripeApiKey.equals("sk_test_mock_key")) {
            log.warn("Using mock Stripe API key. Set payment.stripe.api-key in application.properties for real integration");
        }
        Stripe.apiKey = stripeApiKey;
    }
    
    /**
     * Create a Stripe customer for a user
     */
    public String createCustomer(String email, String name) {
        if (stripeApiKey.equals("sk_test_mock_key")) {
            return "cus_mock_" + System.currentTimeMillis();
        }
        
        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(email)
                .setName(name)
                .build();
                
            Customer customer = Customer.create(params);
            log.info("Created Stripe customer: {}", customer.getId());
            return customer.getId();
        } catch (StripeException e) {
            log.error("Failed to create Stripe customer: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create customer", e);
        }
    }
    
    /**
     * Attach a payment method to a customer
     */
    public void attachPaymentMethod(String paymentMethodId, String customerId) {
        if (stripeApiKey.equals("sk_test_mock_key")) {
            log.info("Mock: Attached payment method {} to customer {}", paymentMethodId, customerId);
            return;
        }
        
        try {
            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
            
            PaymentMethodAttachParams params = PaymentMethodAttachParams.builder()
                .setCustomer(customerId)
                .build();
                
            paymentMethod.attach(params);
            log.info("Attached payment method {} to customer {}", paymentMethodId, customerId);
        } catch (StripeException e) {
            log.error("Failed to attach payment method: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to attach payment method", e);
        }
    }
    
    /**
     * Create a payment intent for deposit
     */
    public String createPaymentIntent(BigDecimal amount, String customerId, String paymentMethodId) {
        if (stripeApiKey.equals("sk_test_mock_key")) {
            return "pi_mock_" + System.currentTimeMillis();
        }
        
        try {
            // Convert amount to cents
            long amountInCents = amount.multiply(new BigDecimal(100)).longValue();
            
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(defaultCurrency.toLowerCase())
                .setCustomer(customerId)
                .setPaymentMethod(paymentMethodId)
                .setConfirm(true)
                .setReturnUrl("https://your-website.com/return")
                .build();
                
            PaymentIntent intent = PaymentIntent.create(params);
            log.info("Created payment intent: {}", intent.getId());
            return intent.getId();
        } catch (StripeException e) {
            log.error("Failed to create payment intent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create payment intent", e);
        }
    }
    
    /**
     * Get payment method details
     */
    public PaymentMethod getPaymentMethod(String paymentMethodId) {
        if (stripeApiKey.equals("sk_test_mock_key")) {
            // Return mock payment method for testing
            return createMockPaymentMethod(paymentMethodId);
        }
        
        try {
            return PaymentMethod.retrieve(paymentMethodId);
        } catch (StripeException e) {
            log.error("Failed to retrieve payment method: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve payment method", e);
        }
    }
    
    /**
     * Confirm payment intent status
     */
    public boolean isPaymentIntentSucceeded(String paymentIntentId) {
        if (stripeApiKey.equals("sk_test_mock_key")) {
            // Mock successful payment for testing
            return true;
        }
        
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            return "succeeded".equals(intent.getStatus());
        } catch (StripeException e) {
            log.error("Failed to check payment intent status: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Create a mock payment method for testing
     */
    private PaymentMethod createMockPaymentMethod(String id) {
        // This is a simplified mock - in real scenarios, you'd use Stripe's test data
        PaymentMethod mockPm = new PaymentMethod();
        mockPm.setId(id);
        mockPm.setType("card");
        
        Map<String, Object> card = new HashMap<>();
        card.put("brand", "visa");
        card.put("last4", "4242");
        card.put("exp_month", 12);
        card.put("exp_year", 2025);
        
        // Note: This is a simplified mock. Real Stripe objects have different structure
        return mockPm;
    }
}