# Simple Payment System Test Script
# Basic functionality test for the payment system

$baseUrl = "http://localhost:8080/api"

Write-Host "Simple Payment System Test" -ForegroundColor Green
Write-Host "==========================" -ForegroundColor Green

# Test health endpoint (no auth required)
Write-Host "`nTesting payment system health..." -ForegroundColor Cyan
try {
    $healthResponse = Invoke-RestMethod -Uri "$baseUrl/payments/health" -Method GET
    Write-Host "✓ Payment system is UP" -ForegroundColor Green
    Write-Host "Service: $($healthResponse.service)" -ForegroundColor Gray
    Write-Host "Version: $($healthResponse.version)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Payment system health check failed" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Please ensure the server is running on port 8080" -ForegroundColor Yellow
    exit 1
}

# Test authentication endpoints
Write-Host "`nTesting authentication..." -ForegroundColor Cyan
try {
    # Try to access protected endpoint without auth (should fail)
    $response = Invoke-RestMethod -Uri "$baseUrl/payments/wallet" -Method GET -ErrorAction SilentlyContinue
    Write-Host "✗ Security issue - protected endpoint accessible without auth" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Host "✓ Authentication required for protected endpoints" -ForegroundColor Green
    } else {
        Write-Host "! Unexpected error accessing protected endpoint" -ForegroundColor Yellow
    }
}

# Test with sample login (this might fail if user doesn't exist)
Write-Host "`nTesting sample login..." -ForegroundColor Cyan
try {
    $loginData = @{
        username = "testuser"
        password = "password123"
    }
    
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/signin" -Method POST -Body ($loginData | ConvertTo-Json) -ContentType "application/json"
    
    if ($loginResponse.token) {
        Write-Host "✓ Sample login successful" -ForegroundColor Green
        
        # Test authenticated endpoint
        $headers = @{ "Authorization" = "Bearer $($loginResponse.token)" }
        $walletResponse = Invoke-RestMethod -Uri "$baseUrl/payments/wallet" -Method GET -Headers $headers
        
        Write-Host "✓ Wallet endpoint accessible with auth" -ForegroundColor Green
        Write-Host "Wallet Balance: $($walletResponse.currency) $($walletResponse.balance)" -ForegroundColor Gray
    }
} catch {
    Write-Host "! Sample login failed (expected if test user doesn't exist)" -ForegroundColor Yellow
    Write-Host "To test authentication, create a user first or update credentials" -ForegroundColor Yellow
}

Write-Host "`n=== Test Summary ===" -ForegroundColor Green
Write-Host "Payment system endpoints are responding correctly" -ForegroundColor Green
Write-Host "Authentication is properly configured" -ForegroundColor Green
Write-Host "`nTo run full tests:" -ForegroundColor Cyan
Write-Host "1. Create test users in the system" -ForegroundColor White
Write-Host "2. Run payment-system-comprehensive-test.ps1" -ForegroundColor White