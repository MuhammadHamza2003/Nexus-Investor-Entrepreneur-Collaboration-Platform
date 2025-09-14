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
    Write-Host "SUCCESS: Send OTP worked!" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor White
    $sendOtpResponse | ConvertTo-Json
    
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
            Start-Sleep -Seconds 1
            $verifyOtpResponse = Invoke-RestMethod -Uri "$baseUrl/auth/verify-otp" -Method POST -Body $verifyOtpBody -ContentType "application/json"
            Write-Host "SUCCESS: Verify OTP worked!" -ForegroundColor Green
            $verifyOtpResponse | ConvertTo-Json
        } catch {
            Write-Host "FAILED: Verify OTP failed!" -ForegroundColor Red
            Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
            if ($_.ErrorDetails.Message) {
                Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
            }
        }
    } else {
        Write-Host "WARNING: No OTP returned in response" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "FAILED: Send OTP failed!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "OTP Testing Complete!" -ForegroundColor Yellow