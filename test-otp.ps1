# Test script for OTP functionality

Write-Host "Testing OTP Functionality..." -ForegroundColor Yellow

$baseUrl = "http://localhost:8080/api"

# Test 1: Send OTP
Write-Host ""
Write-Host "1. Testing Send OTP" -ForegroundColor Cyan
$sendOtpBody = @{
    email = "test@example.com"
    purpose = "LOGIN"
} | ConvertTo-Json

try {
    $sendOtpResponse = Invoke-RestMethod -Uri "$baseUrl/auth/send-otp" -Method POST -Body $sendOtpBody -ContentType "application/json"
    Write-Host "✓ Send OTP Success!" -ForegroundColor Green
    Write-Host "Response: $($sendOtpResponse | ConvertTo-Json)" -ForegroundColor White
    
    if ($sendOtpResponse.otp) {
        $receivedOtp = $sendOtpResponse.otp
        Write-Host "Generated OTP: $receivedOtp" -ForegroundColor Yellow
        
        # Test 2: Verify OTP
        Write-Host ""
        Write-Host "2. Testing Verify OTP" -ForegroundColor Cyan
        $verifyOtpBody = @{
            email = "test@example.com"
            otp = $receivedOtp
            purpose = "LOGIN"
        } | ConvertTo-Json
        
        try {
            Start-Sleep -Seconds 1  # Small delay
            $verifyOtpResponse = Invoke-RestMethod -Uri "$baseUrl/auth/verify-otp" -Method POST -Body $verifyOtpBody -ContentType "application/json"
            Write-Host "✓ Verify OTP Success!" -ForegroundColor Green
            Write-Host "Response: $($verifyOtpResponse | ConvertTo-Json)" -ForegroundColor White
        } catch {
            Write-Host "✗ Verify OTP Failed!" -ForegroundColor Red
            Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
            if ($_.ErrorDetails.Message) {
                Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
            }
        }
    } else {
        Write-Host "⚠ No OTP returned in response" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "✗ Send OTP Failed!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

# Test 3: Invalid OTP
Write-Host ""
Write-Host "3. Testing Invalid OTP" -ForegroundColor Cyan
$invalidOtpBody = @{
    email = "test@example.com"
    otp = "123456"
    purpose = "LOGIN"
} | ConvertTo-Json

try {
    $invalidResponse = Invoke-RestMethod -Uri "$baseUrl/auth/verify-otp" -Method POST -Body $invalidOtpBody -ContentType "application/json"
    Write-Host "⚠ Invalid OTP test didn't fail as expected" -ForegroundColor Yellow
    Write-Host "Response: $($invalidResponse | ConvertTo-Json)" -ForegroundColor White
} catch {
    Write-Host "✓ Invalid OTP correctly rejected" -ForegroundColor Green
    if ($_.ErrorDetails.Message) {
        $errorJson = $_.ErrorDetails.Message | ConvertFrom-Json
        Write-Host "Error message: $($errorJson.message)" -ForegroundColor White
    }
}

Write-Host ""
Write-Host "OTP Testing Complete!" -ForegroundColor Yellow