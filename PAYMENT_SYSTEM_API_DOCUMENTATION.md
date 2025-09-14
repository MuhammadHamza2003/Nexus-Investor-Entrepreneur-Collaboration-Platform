# Payment System API Documentation

**Nexus - Milestone 6: Payment & Transactions**

## Overview

The Payment System provides comprehensive financial transaction capabilities including deposits, withdrawals, transfers between users, and transaction history management. The system integrates with Stripe (in sandbox mode) for payment processing.

## Features Implemented

- ✅ **Deposit Funds**: Add money to user wallets using payment methods
- ✅ **Withdraw Funds**: Transfer money from wallets to external accounts
- ✅ **Transfer Between Users**: Send money between platform users
- ✅ **Transaction History**: Complete audit trail of all transactions
- ✅ **Payment Methods**: Manage saved payment methods (cards, bank accounts)
- ✅ **Wallet Management**: Track balances and spending statistics
- ✅ **Stripe Integration**: Sandbox/mock mode for payment processing
- ✅ **Transaction Status Tracking**: Pending, Completed, Failed states

## API Endpoints

### Authentication

All payment endpoints require JWT authentication. Include the token in the Authorization header:

```
Authorization: Bearer <jwt_token>
```

### Wallet Management

#### GET /api/payments/wallet

Get current user's wallet information including balance and statistics.

**Response:**

```json
{
  "id": "wallet123",
  "balance": 150.0,
  "currency": "USD",
  "totalDeposited": 500.0,
  "totalWithdrawn": 200.0,
  "totalTransferred": 100.0,
  "totalReceived": 50.0,
  "isActive": true,
  "isFrozen": false,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T14:20:00"
}
```

### Payment Methods

#### GET /api/payments/payment-methods

Get all payment methods for the authenticated user.

**Response:**

```json
[
  {
    "id": "pm123",
    "type": "CREDIT_CARD",
    "displayName": "Visa ending in 4242",
    "lastFourDigits": "4242",
    "brand": "visa",
    "formattedExpiration": "12/25",
    "isDefault": true,
    "isActive": true,
    "isExpired": false,
    "createdAt": "2024-01-15T10:00:00"
  }
]
```

#### POST /api/payments/payment-methods

Add a new payment method.

**Request Body:**

```json
{
  "type": "CREDIT_CARD",
  "paymentToken": "pm_test_card_token",
  "displayName": "My Visa Card",
  "setAsDefault": true
}
```

#### DELETE /api/payments/payment-methods/{id}

Delete a payment method.

#### PUT /api/payments/payment-methods/{id}/default

Set a payment method as default.

### Transactions

#### POST /api/payments/deposit

Deposit funds into the user's wallet.

**Request Body:**

```json
{
  "amount": 100.0,
  "paymentMethodId": "pm123",
  "description": "Wallet deposit"
}
```

**Response:**

```json
{
  "id": "tx123",
  "type": "DEPOSIT",
  "amount": 100.0,
  "status": "COMPLETED",
  "description": "Wallet deposit",
  "paymentMethod": "CREDIT_CARD",
  "currency": "USD",
  "fee": 3.2,
  "netAmount": 96.8,
  "createdAt": "2024-01-15T14:30:00",
  "completedAt": "2024-01-15T14:30:05"
}
```

#### POST /api/payments/withdraw

Withdraw funds from the user's wallet.

**Request Body:**

```json
{
  "amount": 50.0,
  "paymentMethodId": "pm123",
  "description": "Cash withdrawal"
}
```

#### POST /api/payments/transfer

Transfer funds to another user.

**Request Body:**

```json
{
  "amount": 25.0,
  "recipientUsername": "investor1",
  "description": "Investment payment"
}
```

#### GET /api/payments/transactions

Get all transactions for the authenticated user.

#### GET /api/payments/transactions/paginated?page=0&size=10

Get paginated transaction history.

#### GET /api/payments/transactions/status/{status}

Get transactions by status (PENDING, COMPLETED, FAILED, CANCELLED).

#### GET /api/payments/transactions/{id}

Get details of a specific transaction.

### System Health

#### GET /api/payments/health

Check payment system health status.

**Response:**

```json
{
  "status": "UP",
  "service": "Payment System",
  "version": "1.0.0",
  "timestamp": "2024-01-15T14:30:00"
}
```

## Transaction Types

- **DEPOSIT**: Adding funds to wallet from external source
- **WITHDRAW**: Removing funds from wallet to external account
- **TRANSFER**: Moving funds between platform users
- **INVESTMENT**: Investment-related transactions
- **REFUND**: Refunded transactions

## Transaction Statuses

- **PENDING**: Transaction initiated but not yet processed
- **PROCESSING**: Transaction being processed by payment gateway
- **COMPLETED**: Transaction successfully completed
- **FAILED**: Transaction failed due to error
- **CANCELLED**: Transaction was cancelled

## Payment Method Types

- **STRIPE**: Stripe payment method
- **PAYPAL**: PayPal account
- **CREDIT_CARD**: Credit card
- **DEBIT_CARD**: Debit card
- **BANK_TRANSFER**: Bank transfer

## Fee Structure

- **Transaction Fee**: 2.9% of amount
- **Fixed Fee**: $0.30 per transaction
- **Transfer Fee**: No fee for user-to-user transfers

## Error Handling

All endpoints return appropriate HTTP status codes and error messages:

**400 Bad Request**

```json
{
  "error": "Validation failed",
  "message": "Amount must be greater than $1.00"
}
```

**401 Unauthorized**

```json
{
  "error": "Authentication required",
  "message": "JWT token is missing or invalid"
}
```

**404 Not Found**

```json
{
  "error": "Resource not found",
  "message": "Payment method not found"
}
```

**500 Internal Server Error**

```json
{
  "error": "Internal server error",
  "message": "Payment processing failed"
}
```

## Security Features

- JWT-based authentication required for all endpoints
- User can only access their own transactions and payment methods
- Payment method tokens are securely stored
- Transaction amounts are validated
- Insufficient balance checks for withdrawals and transfers

## Testing

Use the provided test scripts:

- `payment-system-simple-test.ps1` - Basic health and connectivity test
- `payment-system-comprehensive-test.ps1` - Full API functionality test

## Configuration

Update `application.properties` for production:

```properties
# Stripe Configuration
payment.stripe.api-key=sk_live_your_stripe_key
payment.stripe.webhook-secret=whsec_your_webhook_secret

# Fee Configuration
payment.transaction-fee-percentage=0.029
payment.fixed-fee=0.30
payment.default-currency=USD
```

## Database Schema

The system uses MongoDB with the following collections:

- `wallets` - User wallet information
- `transactions` - Transaction records
- `payment_methods` - Saved payment methods
- `users` - User accounts (existing)

## Integration Notes

- Currently configured for Stripe sandbox/test mode
- Mock payment processing for development
- Ready for production Stripe integration
- Supports webhook integration for async payment processing
- Extensible for additional payment gateways (PayPal, etc.)

## Next Steps for Production

1. Configure real Stripe API keys
2. Implement Stripe webhooks for async processing
3. Add PayPal integration
4. Implement advanced fraud detection
5. Add comprehensive audit logging
6. Set up monitoring and alerting
