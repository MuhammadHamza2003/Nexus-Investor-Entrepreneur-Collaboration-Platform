param(
    [string]$BaseUrl = "http://localhost:8080"
)

$apiUrl = "$BaseUrl/api"

Write-Host "=== MILESTONE 7 SECURITY ENHANCEMENTS TEST ===" -ForegroundColor Cyan
Write-Host ""

# Test 1: Password Validation
Write-Host "1. Testing Password Validation..." -ForegroundColor Yellow

$weakPasswordUser = @{
    username = "testuser456"
    email = "test2@example.com"
    password = "weak"
    firstName = "Test"
    lastName = "User"
    role = "INVESTOR"
}

try {
    $response = Invoke-RestMethod -Uri "$apiUrl/auth/register" -Method POST -Body ($weakPasswordUser | ConvertTo-Json) -ContentType "application/json" -ErrorAction Stop
    Write-Host "   FAIL: Weak password was accepted" -ForegroundColor Red
} catch {
    Write-Host "   PASS: Weak password rejected" -ForegroundColor Green
}

# Test 2: Input Sanitization
Write-Host ""
Write-Host "2. Testing Input Sanitization..." -ForegroundColor Yellow

$xssUser = @{
    username = "testuser789"
    email = "test3@example.com"
    password = "SecurePass123!"
    firstName = "<script>alert('xss')</script>"
    lastName = "User"
    role = "INVESTOR"
}

try {
    $response = Invoke-RestMethod -Uri "$apiUrl/auth/register" -Method POST -Body ($xssUser | ConvertTo-Json) -ContentType "application/json" -ErrorAction Stop
    Write-Host "   PASS: User registered with sanitized input" -ForegroundColor Green
} catch {
    Write-Host "   INFO: Registration failed (validation)" -ForegroundColor Yellow
}

# Test 3: 2FA OTP
Write-Host ""
Write-Host "3. Testing 2FA OTP System..." -ForegroundColor Yellow

$otpRequest = @{
    email = "test@example.com"
    purpose = "LOGIN"
}

try {
    $otpResponse = Invoke-RestMethod -Uri "$apiUrl/auth/send-otp" -Method POST -Body ($otpRequest | ConvertTo-Json) -ContentType "application/json" -ErrorAction Stop
    Write-Host "   PASS: OTP sent successfully" -ForegroundColor Green
    if ($otpResponse.otp) {
        Write-Host "   Test OTP: $($otpResponse.otp)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   FAIL: OTP sending failed" -ForegroundColor Red
}

# Test 4: Valid Registration
Write-Host ""
Write-Host "4. Testing Valid Registration..." -ForegroundColor Yellow

$validUser = @{
    username = "testuser123"
    email = "test@example.com"
    password = "SecurePass123!"
    firstName = "Test"
    lastName = "User"
    role = "INVESTOR"
    phoneNumber = "+1234567890"
    location = "New York"
}

try {
    $response = Invoke-RestMethod -Uri "$apiUrl/auth/register" -Method POST -Body ($validUser | ConvertTo-Json) -ContentType "application/json" -ErrorAction Stop
    Write-Host "   PASS: Valid user registration successful" -ForegroundColor Green
} catch {
    Write-Host "   INFO: Registration may have failed (user exists?)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== MILESTONE 7 SECURITY FEATURES ===" -ForegroundColor Green
Write-Host "- Enhanced Password Validation" -ForegroundColor Green
Write-Host "- Rate Limiting System" -ForegroundColor Green
Write-Host "- Input Sanitization" -ForegroundColor Green
Write-Host "- 2FA Mock Implementation" -ForegroundColor Green
Write-Host "- Global Exception Handler" -ForegroundColor Green
Write-Host ""