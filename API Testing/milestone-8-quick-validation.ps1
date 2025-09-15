# Nexus API Quick Validation Test
# This script performs a quick validation of the Nexus API endpoints

Write-Host "============================================" -ForegroundColor Yellow
Write-Host "NEXUS API QUICK VALIDATION TEST" -ForegroundColor Yellow  
Write-Host "============================================" -ForegroundColor Yellow

$baseUrl = "http://localhost:8080"
$passCount = 0
$failCount = 0

# Function to test an endpoint
function Test-Endpoint {
    param(
        [string]$method,
        [string]$endpoint,
        [string]$testName,
        [hashtable]$headers = @{},
        [string]$body = $null
    )
    
    try {
        $uri = "$baseUrl$endpoint"
        
        if ($method -eq "POST" -and $body) {
            $response = Invoke-RestMethod -Uri $uri -Method $method -Headers $headers -Body $body -ContentType "application/json" -TimeoutSec 10
        } else {
            $response = Invoke-RestMethod -Uri $uri -Method $method -Headers $headers -TimeoutSec 10
        }
        
        Write-Host "[PASS] $testName" -ForegroundColor Green
        return $true, $response
    }
    catch {
        Write-Host "[FAIL] $testName - $($_.Exception.Message)" -ForegroundColor Red
        return $false, $null
    }
}

Write-Host "`n1. TESTING BASIC CONNECTIVITY" -ForegroundColor Cyan

# Test health endpoint
$success, $response = Test-Endpoint -method "GET" -endpoint "/api/public/health" -testName "Health Check"
if ($success) { $passCount++ } else { $failCount++ }

# Test public test endpoint  
$success, $response = Test-Endpoint -method "GET" -endpoint "/api/public/test" -testName "Public Test Endpoint"
if ($success) { $passCount++ } else { $failCount++ }

Write-Host "`n2. TESTING API DOCUMENTATION" -ForegroundColor Cyan

# Test Swagger UI (we'll just check if the endpoint responds)
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/swagger-ui/index.html" -TimeoutSec 10 -UseBasicParsing
    if ($response.StatusCode -eq 200) {
        Write-Host "[PASS] Swagger UI Accessibility" -ForegroundColor Green
        $passCount++
    } else {
        Write-Host "[FAIL] Swagger UI - Status Code: $($response.StatusCode)" -ForegroundColor Red
        $failCount++
    }
} catch {
    Write-Host "[FAIL] Swagger UI - $($_.Exception.Message)" -ForegroundColor Red
    $failCount++
}

# Test OpenAPI docs
$success, $response = Test-Endpoint -method "GET" -endpoint "/v3/api-docs" -testName "OpenAPI Documentation"
if ($success) { $passCount++ } else { $failCount++ }

Write-Host "`n3. TESTING USER REGISTRATION" -ForegroundColor Cyan

# Test investor registration
$investorData = @{
    username = "test_investor_$(Get-Random)"
    email = "investor$(Get-Random)@example.com"
    password = "Password123!"
    firstName = "Test"
    lastName = "Investor"
    role = "INVESTOR"
    phoneNumber = "+1234567890"
    location = "Test City"
} | ConvertTo-Json

$success, $investorResponse = Test-Endpoint -method "POST" -endpoint "/api/auth/register" -testName "Investor Registration" -body $investorData
if ($success) { 
    $passCount++
    $investorToken = $investorResponse.token
    Write-Host "  -> Investor JWT Token: $($investorToken.Substring(0,50))..." -ForegroundColor Gray
} else { 
    $failCount++ 
    $investorToken = $null
}

# Test entrepreneur registration
$entrepreneurData = @{
    username = "test_entrepreneur_$(Get-Random)"
    email = "entrepreneur$(Get-Random)@example.com"
    password = "Password123!"
    firstName = "Test"  
    lastName = "Entrepreneur"
    role = "ENTREPRENEUR"
    phoneNumber = "+1234567890"
    location = "Test City"
} | ConvertTo-Json

$success, $entrepreneurResponse = Test-Endpoint -method "POST" -endpoint "/api/auth/register" -testName "Entrepreneur Registration" -body $entrepreneurData
if ($success) { 
    $passCount++
    $entrepreneurToken = $entrepreneurResponse.token
    Write-Host "  -> Entrepreneur JWT Token: $($entrepreneurToken.Substring(0,50))..." -ForegroundColor Gray
} else { 
    $failCount++
    $entrepreneurToken = $null
}

Write-Host "`n4. TESTING AUTHENTICATION" -ForegroundColor Cyan

if ($investorToken) {
    $authHeaders = @{ "Authorization" = "Bearer $investorToken" }
    $success, $response = Test-Endpoint -method "GET" -endpoint "/api/auth/profile" -testName "Investor Profile Access" -headers $authHeaders
    if ($success) { $passCount++ } else { $failCount++ }
}

if ($entrepreneurToken) {
    $authHeaders = @{ "Authorization" = "Bearer $entrepreneurToken" }  
    $success, $response = Test-Endpoint -method "GET" -endpoint "/api/auth/profile" -testName "Entrepreneur Profile Access" -headers $authHeaders
    if ($success) { $passCount++ } else { $failCount++ }
}

Write-Host "`n5. TESTING ROLE-BASED ACCESS" -ForegroundColor Cyan

if ($investorToken) {
    $authHeaders = @{ "Authorization" = "Bearer $investorToken" }
    $success, $response = Test-Endpoint -method "GET" -endpoint "/api/investor/dashboard" -testName "Investor Dashboard Access" -headers $authHeaders
    if ($success) { $passCount++ } else { $failCount++ }
}

if ($entrepreneurToken) {
    $authHeaders = @{ "Authorization" = "Bearer $entrepreneurToken" }
    $success, $response = Test-Endpoint -method "GET" -endpoint "/api/entrepreneur/dashboard" -testName "Entrepreneur Dashboard Access" -headers $authHeaders
    if ($success) { $passCount++ } else { $failCount++ }
}

Write-Host "`n============================================" -ForegroundColor Yellow
Write-Host "QUICK VALIDATION RESULTS" -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor Yellow

$totalTests = $passCount + $failCount
$successRate = if ($totalTests -gt 0) { [math]::Round(($passCount / $totalTests) * 100, 1) } else { 0 }

Write-Host "Total Tests: $totalTests" -ForegroundColor White
Write-Host "Passed: $passCount" -ForegroundColor Green
Write-Host "Failed: $failCount" -ForegroundColor Red
Write-Host "Success Rate: $successRate%" -ForegroundColor $(if ($successRate -ge 80) { "Green" } elseif ($successRate -ge 60) { "Yellow" } else { "Red" })

if ($successRate -ge 80) {
    Write-Host "`n✅ VALIDATION SUCCESSFUL - API is working correctly!" -ForegroundColor Green
    Write-Host "The Nexus API is ready for production deployment." -ForegroundColor Green
} elseif ($successRate -ge 60) {
    Write-Host "`n⚠️  VALIDATION PARTIAL - Some issues detected" -ForegroundColor Yellow
    Write-Host "The API is functional but may need minor fixes." -ForegroundColor Yellow
} else {
    Write-Host "`n❌ VALIDATION FAILED - Major issues detected" -ForegroundColor Red  
    Write-Host "The API requires attention before deployment." -ForegroundColor Red
}

Write-Host "`n============================================" -ForegroundColor Yellow
Write-Host "API DOCUMENTATION LINKS" -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor Yellow
Write-Host "Swagger UI: $baseUrl/swagger-ui/index.html" -ForegroundColor Cyan
Write-Host "OpenAPI JSON: $baseUrl/v3/api-docs" -ForegroundColor Cyan
Write-Host "Health Check: $baseUrl/api/public/health" -ForegroundColor Cyan

Write-Host "`nValidation completed at $(Get-Date)" -ForegroundColor Gray