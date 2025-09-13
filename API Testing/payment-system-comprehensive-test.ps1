# Payment System API Testing Script
# This script tests all payment-related endpoints

$baseUrl = "http://localhost:8080/api"
$authToken = ""

Write-Host "Nexus Payment System API Testing Script" -ForegroundColor Green
Write-Host "=======================================" -ForegroundColor Green

# Function to make authenticated API requests
function Invoke-AuthenticatedRequest {
    param(
        [string]$Method,
        [string]$Uri,
        [hashtable]$Body = $null,
        [string]$ContentType = "application/json"
    )
    
    $headers = @{
        "Authorization" = "Bearer $authToken"
        "Content-Type" = $ContentType
    }
    
    try {
        if ($Body) {
            $jsonBody = $Body | ConvertTo-Json -Depth 10
            Write-Host "Request Body: $jsonBody" -ForegroundColor Yellow
            return Invoke-RestMethod -Uri $Uri -Method $Method -Headers $headers -Body $jsonBody
        } else {
            return Invoke-RestMethod -Uri $Uri -Method $Method -Headers $headers
        }
    } catch {
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
            $errorBody = $reader.ReadToEnd()
            Write-Host "Error Body: $errorBody" -ForegroundColor Red
        }
        return $null
    }
}

# Step 1: Authentication (Login)
Write-Host "`n1. Authenticating user..." -ForegroundColor Cyan

try {
    $loginRequest = @{
        username = "testuser"
        password = "password123"
    }
    
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/signin" -Method POST -Body ($loginRequest | ConvertTo-Json) -ContentType "application/json"
    
    if ($loginResponse.token) {
        $authToken = $loginResponse.token
        Write-Host "Authentication successful!" -ForegroundColor Green
        Write-Host "Token: $($authToken.Substring(0, 20))..." -ForegroundColor Gray
    } else {
        Write-Host "Authentication failed - no token received" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "Authentication failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Please ensure the server is running and user credentials are correct" -ForegroundColor Yellow
    exit 1
}

# Step 2: Test Payment System Health
Write-Host "`n2. Testing payment system health..." -ForegroundColor Cyan

$healthResponse = Invoke-AuthenticatedRequest -Method GET -Uri "$baseUrl/payments/health"
if ($healthResponse) {
    Write-Host "Payment system status: $($healthResponse.status)" -ForegroundColor Green
    Write-Host "Service: $($healthResponse.service)" -ForegroundColor Gray
    Write-Host "Version: $($healthResponse.version)" -ForegroundColor Gray
}

# Step 3: Get User Wallet
Write-Host "`n3. Getting user wallet..." -ForegroundColor Cyan

$walletResponse = Invoke-AuthenticatedRequest -Method GET -Uri "$baseUrl/payments/wallet"
if ($walletResponse) {
    Write-Host "Wallet Balance: $($walletResponse.currency) $($walletResponse.balance)" -ForegroundColor Green
    Write-Host "Total Deposited: $($walletResponse.currency) $($walletResponse.totalDeposited)" -ForegroundColor Gray
    Write-Host "Total Withdrawn: $($walletResponse.currency) $($walletResponse.totalWithdrawn)" -ForegroundColor Gray
    Write-Host "Wallet Status: Active=$($walletResponse.isActive), Frozen=$($walletResponse.isFrozen)" -ForegroundColor Gray
}

# Step 4: Get Payment Methods
Write-Host "`n4. Getting user payment methods..." -ForegroundColor Cyan

$paymentMethodsResponse = Invoke-AuthenticatedRequest -Method GET -Uri "$baseUrl/payments/payment-methods"
if ($paymentMethodsResponse) {
    Write-Host "Found $($paymentMethodsResponse.Count) payment methods" -ForegroundColor Green
    foreach ($pm in $paymentMethodsResponse) {
        Write-Host "  - $($pm.displayName) (Type: $($pm.type), Default: $($pm.isDefault))" -ForegroundColor Gray
    }
}

# Step 5: Add a Test Payment Method (Mock)
Write-Host "`n5. Adding a test payment method..." -ForegroundColor Cyan

$addPaymentMethodRequest = @{
    type = "CREDIT_CARD"
    paymentToken = "pm_test_mock_$(Get-Random)"
    displayName = "Test Visa Card"
    setAsDefault = $true
}

$addPaymentMethodResponse = Invoke-AuthenticatedRequest -Method POST -Uri "$baseUrl/payments/payment-methods" -Body $addPaymentMethodRequest
if ($addPaymentMethodResponse) {
    Write-Host "Payment method added successfully!" -ForegroundColor Green
    Write-Host "Payment Method ID: $($addPaymentMethodResponse.id)" -ForegroundColor Gray
    $testPaymentMethodId = $addPaymentMethodResponse.id
}

# Step 6: Test Deposit
Write-Host "`n6. Testing deposit..." -ForegroundColor Cyan

$depositRequest = @{
    amount = 100.00
    paymentMethodId = $testPaymentMethodId
    description = "Test deposit via API"
}

$depositResponse = Invoke-AuthenticatedRequest -Method POST -Uri "$baseUrl/payments/deposit" -Body $depositRequest
if ($depositResponse) {
    Write-Host "Deposit transaction created!" -ForegroundColor Green
    Write-Host "Transaction ID: $($depositResponse.id)" -ForegroundColor Gray
    Write-Host "Amount: $($depositResponse.currency) $($depositResponse.amount)" -ForegroundColor Gray
    Write-Host "Status: $($depositResponse.status)" -ForegroundColor Gray
    Write-Host "Net Amount: $($depositResponse.currency) $($depositResponse.netAmount)" -ForegroundColor Gray
}

# Step 7: Check Updated Wallet Balance
Write-Host "`n7. Checking updated wallet balance..." -ForegroundColor Cyan

$updatedWalletResponse = Invoke-AuthenticatedRequest -Method GET -Uri "$baseUrl/payments/wallet"
if ($updatedWalletResponse) {
    Write-Host "Updated Balance: $($updatedWalletResponse.currency) $($updatedWalletResponse.balance)" -ForegroundColor Green
}

# Step 8: Test Withdrawal
Write-Host "`n8. Testing withdrawal..." -ForegroundColor Cyan

$withdrawRequest = @{
    amount = 25.00
    paymentMethodId = $testPaymentMethodId
    description = "Test withdrawal via API"
}

$withdrawResponse = Invoke-AuthenticatedRequest -Method POST -Uri "$baseUrl/payments/withdraw" -Body $withdrawRequest
if ($withdrawResponse) {
    Write-Host "Withdrawal transaction created!" -ForegroundColor Green
    Write-Host "Transaction ID: $($withdrawResponse.id)" -ForegroundColor Gray
    Write-Host "Amount: $($withdrawResponse.currency) $($withdrawResponse.amount)" -ForegroundColor Gray
    Write-Host "Status: $($withdrawResponse.status)" -ForegroundColor Gray
}

# Step 9: Test Transfer (this will fail if recipient doesn't exist)
Write-Host "`n9. Testing transfer..." -ForegroundColor Cyan

$transferRequest = @{
    amount = 10.00
    recipientUsername = "investor1"
    description = "Test transfer via API"
}

$transferResponse = Invoke-AuthenticatedRequest -Method POST -Uri "$baseUrl/payments/transfer" -Body $transferRequest
if ($transferResponse) {
    Write-Host "Transfer transaction created!" -ForegroundColor Green
    Write-Host "Transaction ID: $($transferResponse.id)" -ForegroundColor Gray
    Write-Host "Amount: $($transferResponse.currency) $($transferResponse.amount)" -ForegroundColor Gray
    Write-Host "Recipient: $($transferResponse.recipientUsername)" -ForegroundColor Gray
    Write-Host "Status: $($transferResponse.status)" -ForegroundColor Gray
} else {
    Write-Host "Transfer failed (expected if recipient doesn't exist)" -ForegroundColor Yellow
}

# Step 10: Get Transaction History
Write-Host "`n10. Getting transaction history..." -ForegroundColor Cyan

$transactionsResponse = Invoke-AuthenticatedRequest -Method GET -Uri "$baseUrl/payments/transactions"
if ($transactionsResponse) {
    Write-Host "Found $($transactionsResponse.Count) transactions" -ForegroundColor Green
    foreach ($tx in $transactionsResponse) {
        Write-Host "  - $($tx.type): $($tx.currency) $($tx.amount) ($($tx.status))" -ForegroundColor Gray
        Write-Host "    Description: $($tx.description)" -ForegroundColor DarkGray
        Write-Host "    Created: $($tx.createdAt)" -ForegroundColor DarkGray
    }
}

# Step 11: Get Transactions by Status
Write-Host "`n11. Getting completed transactions..." -ForegroundColor Cyan

$completedTransactionsResponse = Invoke-AuthenticatedRequest -Method GET -Uri "$baseUrl/payments/transactions/status/COMPLETED"
if ($completedTransactionsResponse) {
    Write-Host "Found $($completedTransactionsResponse.Count) completed transactions" -ForegroundColor Green
}

# Step 12: Get Paginated Transactions
Write-Host "`n12. Getting paginated transactions..." -ForegroundColor Cyan

$paginatedTransactionsResponse = Invoke-AuthenticatedRequest -Method GET -Uri "$baseUrl/payments/transactions/paginated?page=0&size=5"
if ($paginatedTransactionsResponse) {
    Write-Host "Page size: $($paginatedTransactionsResponse.size)" -ForegroundColor Green
    Write-Host "Total elements: $($paginatedTransactionsResponse.totalElements)" -ForegroundColor Gray
    Write-Host "Total pages: $($paginatedTransactionsResponse.totalPages)" -ForegroundColor Gray
}

# Step 13: Test Error Scenarios
Write-Host "`n13. Testing error scenarios..." -ForegroundColor Cyan

# Test insufficient funds withdrawal
Write-Host "  Testing withdrawal with insufficient funds..." -ForegroundColor Yellow
$largeWithdrawRequest = @{
    amount = 10000.00
    paymentMethodId = $testPaymentMethodId
    description = "Test large withdrawal"
}

$largeWithdrawResponse = Invoke-AuthenticatedRequest -Method POST -Uri "$baseUrl/payments/withdraw" -Body $largeWithdrawRequest
if ($largeWithdrawResponse -and $largeWithdrawResponse.status -eq "FAILED") {
    Write-Host "  ✓ Insufficient funds error handled correctly" -ForegroundColor Green
}

# Test invalid payment method
Write-Host "  Testing with invalid payment method..." -ForegroundColor Yellow
$invalidDepositRequest = @{
    amount = 50.00
    paymentMethodId = "invalid_pm_id"
    description = "Test invalid deposit"
}

$invalidDepositResponse = Invoke-AuthenticatedRequest -Method POST -Uri "$baseUrl/payments/deposit" -Body $invalidDepositRequest
if ($null -eq $invalidDepositResponse) {
    Write-Host "  ✓ Invalid payment method error handled correctly" -ForegroundColor Green
}

Write-Host "`n=== Payment System API Test Completed ===" -ForegroundColor Green
Write-Host "All major endpoints have been tested!" -ForegroundColor Green
Write-Host "Check the console output above for detailed results." -ForegroundColor Yellow

Write-Host "`nNext steps:" -ForegroundColor Cyan
Write-Host "1. Review transaction history in the application" -ForegroundColor White
Write-Host "2. Test with real Stripe integration (update API keys in application.properties)" -ForegroundColor White
Write-Host "3. Test with multiple users for transfer functionality" -ForegroundColor White
Write-Host "4. Monitor wallet balances and transaction integrity" -ForegroundColor White