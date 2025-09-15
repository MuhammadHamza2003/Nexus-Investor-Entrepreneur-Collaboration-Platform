# ===================================================================
# NEXUS API COMPREHENSIVE TEST SUITE - MILESTONE 8 FINAL
# ===================================$result = Test-Endpoint "$baseUrl/api/auth/login" "POST" $loginData
if ($result.Success -and $result.Data.token) {
    $tokens.loginTest = $result.Data.token
    Add-Result "Valid Login" "PASS" "Login successful with valid credentials"
} else {
    Add-Result "Valid Login" "FAIL" "Login failed: $($result.Error)"
}

# Add delay to avoid rate limiting
Start-Sleep -Seconds 3

# Test Invalid Login=======================
# This script tests all authentication and user management APIs
# for the Nexus Investor-Entrepreneur Collaboration Platform
# ===================================================================

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "NEXUS API COMPREHENSIVE TEST SUITE - FINAL" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080"
$randomId = Get-Random -Maximum 999999
$global:results = @()
$tokens = @{}

function Add-Result($test, $status, $details) {
    $global:results += "$status`: $test - $details"
    if ($status -eq "PASS") {
        Write-Host "[PASS] $test - $details" -ForegroundColor Green
    } else {
        Write-Host "[FAIL] $test - $details" -ForegroundColor Red
    }
}

function Test-Endpoint($url, $method = "GET", $body = $null, $headers = @{}) {
    try {
        $params = @{
            Uri = $url
            Method = $method
            Headers = $headers
            ContentType = "application/json"
        }
        
        if ($body) {
            $params.Body = $body
        }
        
        $response = Invoke-RestMethod @params
        return @{ Success = $true; Data = $response; StatusCode = 200 }
    }
    catch {
        $statusCode = 0
        if ($_.Exception.Response) {
            $statusCode = [int]$_.Exception.Response.StatusCode
        }
        return @{ Success = $false; Error = $_.Exception.Message; StatusCode = $statusCode }
    }
}

# ===================================================================
# 1. PUBLIC ENDPOINTS TESTING
# ===================================================================
Write-Host "`n1. TESTING PUBLIC ENDPOINTS" -ForegroundColor Yellow

# Test Health Check
$result = Test-Endpoint "$baseUrl/api/public/health"
if ($result.Success -and $result.Data.message) {
    Add-Result "Health Check" "PASS" "Service is healthy: $($result.Data.message)"
} else {
    Add-Result "Health Check" "FAIL" "Health endpoint failed: $($result.Error)"
}

# Test Public Test Endpoint
$result = Test-Endpoint "$baseUrl/api/public/test"
if ($result.Success -and $result.Data.message) {
    Add-Result "Public Test" "PASS" "Public endpoint accessible: $($result.Data.message)"
} else {
    Add-Result "Public Test" "FAIL" "Public test endpoint failed: $($result.Error)"
}

# ===================================================================
# 2. API DOCUMENTATION TESTING
# ===================================================================
Write-Host "`n2. TESTING API DOCUMENTATION" -ForegroundColor Yellow

# Test Swagger UI
try {
    $swaggerResponse = Invoke-WebRequest -Uri "$baseUrl/swagger-ui/index.html" -Method GET
    if ($swaggerResponse.StatusCode -eq 200) {
        Add-Result "Swagger UI" "PASS" "Swagger UI is accessible"
    } else {
        Add-Result "Swagger UI" "FAIL" "Swagger UI returned status: $($swaggerResponse.StatusCode)"
    }
} catch {
    Add-Result "Swagger UI" "FAIL" "Swagger UI not accessible: $($_.Exception.Message)"
}

# Test OpenAPI JSON
$result = Test-Endpoint "$baseUrl/v3/api-docs"
if ($result.Success -and $result.Data.openapi) {
    Add-Result "OpenAPI Docs" "PASS" "OpenAPI specification available"
} else {
    Add-Result "OpenAPI Docs" "FAIL" "OpenAPI docs failed: $($result.Error)"
}

# ===================================================================
# 3. USER REGISTRATION TESTING
# ===================================================================
Write-Host "`n3. TESTING USER REGISTRATION" -ForegroundColor Yellow

# Register Investor
$investorData = @{
    username = "test_investor_$randomId"
    email = "investor$randomId@nexus.com"
    password = "Password123!"
    firstName = "John"
    lastName = "Investor"
    role = "INVESTOR"
    phoneNumber = "+1234567890"
    location = "New York, USA"
} | ConvertTo-Json

$result = Test-Endpoint "$baseUrl/api/auth/register" "POST" $investorData
if ($result.Success -and $result.Data.token) {
    $tokens.investor = $result.Data.token
    Add-Result "Investor Registration" "PASS" "Investor registered with token"
} else {
    Add-Result "Investor Registration" "FAIL" "Registration failed: $($result.Error)"
}

# Add delay to avoid rate limiting
Start-Sleep -Seconds 3

# Register Entrepreneur
$entrepreneurData = @{
    username = "test_entrepreneur_$randomId"
    email = "entrepreneur$randomId@nexus.com"
    password = "Password123!"
    firstName = "Jane"
    lastName = "Entrepreneur"
    role = "ENTREPRENEUR"
    phoneNumber = "+1234567891"
    location = "San Francisco, USA"
} | ConvertTo-Json

$result = Test-Endpoint "$baseUrl/api/auth/register" "POST" $entrepreneurData
if ($result.Success -and $result.Data.token) {
    $tokens.entrepreneur = $result.Data.token
    Add-Result "Entrepreneur Registration" "PASS" "Entrepreneur registered with token"
} else {
    Add-Result "Entrepreneur Registration" "FAIL" "Registration failed: $($result.Error)"
}

# Add delay to avoid rate limiting
Start-Sleep -Seconds 3

# Test Duplicate Registration
$result = Test-Endpoint "$baseUrl/api/auth/register" "POST" $investorData
if (!$result.Success -and $result.StatusCode -eq 400) {
    Add-Result "Duplicate Registration" "PASS" "Duplicate registration properly rejected"
} else {
    Add-Result "Duplicate Registration" "FAIL" "Duplicate registration should be rejected"
}

# ===================================================================
# 4. USER LOGIN TESTING
# ===================================================================
Write-Host "`n4. TESTING USER LOGIN" -ForegroundColor Yellow

# Valid Login
$loginData = @{
    usernameOrEmail = "test_investor_$randomId"
    password = "Password123!"
} | ConvertTo-Json

$result = Test-Endpoint "$baseUrl/api/auth/login" "POST" $loginData
if ($result.Success -and $result.Data.token) {
    $tokens.login = $result.Data.token
    Add-Result "Valid Login" "PASS" "Login successful with valid credentials"
} else {
    Add-Result "Valid Login" "FAIL" "Login failed: $($result.Error)"
}

# Invalid Login
$invalidLoginData = @{
    usernameOrEmail = "invalid_user"
    password = "wrong_password"
} | ConvertTo-Json

$result = Test-Endpoint "$baseUrl/api/auth/login" "POST" $invalidLoginData
if (!$result.Success -and $result.StatusCode -eq 400) {
    Add-Result "Invalid Login" "PASS" "Invalid login properly rejected"
} else {
    Add-Result "Invalid Login" "FAIL" "Invalid login should be rejected"
}

# ===================================================================
# 5. PROFILE MANAGEMENT TESTING
# ===================================================================
Write-Host "`n5. TESTING PROFILE MANAGEMENT" -ForegroundColor Yellow

if ($tokens.investor) {
    $headers = @{ "Authorization" = "Bearer $($tokens.investor)" }
    
    # Get Profile
    $result = Test-Endpoint "$baseUrl/api/auth/profile" "GET" $null $headers
    if ($result.Success -and $result.Data.username) {
        Add-Result "Get Profile" "PASS" "Profile retrieved successfully"
    } else {
        Add-Result "Get Profile" "FAIL" "Profile retrieval failed: $($result.Error)"
    }
    
    # Update Profile
    $updateData = @{
        bio = "Updated bio for testing"
        portfolio = "https://portfolio.example.com"
        preferences = "AI, Fintech, Healthcare"
        location = "Updated Location, USA"
    } | ConvertTo-Json
    
    $result = Test-Endpoint "$baseUrl/api/auth/profile" "PUT" $updateData $headers
    if ($result.Success -and $result.Data.bio -like "*Updated*") {
        Add-Result "Update Profile" "PASS" "Profile updated successfully"
    } else {
        Add-Result "Update Profile" "FAIL" "Profile update failed: $($result.Error)"
    }
}

# Test Unauthorized Profile Access
$result = Test-Endpoint "$baseUrl/api/auth/profile" "GET"
if (!$result.Success -and ($result.StatusCode -eq 400 -or $result.StatusCode -eq 401 -or $result.StatusCode -eq 403)) {
    Add-Result "Unauthorized Profile Access" "PASS" "Unauthorized access properly blocked"
} else {
    Add-Result "Unauthorized Profile Access" "FAIL" "Should block unauthorized access"
}

# ===================================================================
# 6. ROLE-BASED ACCESS CONTROL TESTING
# ===================================================================
Write-Host "`n6. TESTING ROLE-BASED ACCESS CONTROL" -ForegroundColor Yellow

# Investor Dashboard Access
if ($tokens.investor) {
    $headers = @{ "Authorization" = "Bearer $($tokens.investor)" }
    $result = Test-Endpoint "$baseUrl/api/investor/dashboard" "GET" $null $headers
    if ($result.Success) {
        Add-Result "Investor Dashboard Access" "PASS" "Investor can access dashboard"
    } else {
        Add-Result "Investor Dashboard Access" "FAIL" "Investor dashboard access failed: $($result.Error)"
    }
    
    # Try Entrepreneur Endpoint with Investor Token
    $result = Test-Endpoint "$baseUrl/api/entrepreneur/dashboard" "GET" $null $headers
    if (!$result.Success -and $result.StatusCode -eq 403) {
        Add-Result "Cross-Role Access Control" "PASS" "Investor blocked from entrepreneur dashboard"
    } else {
        Add-Result "Cross-Role Access Control" "FAIL" "Should block cross-role access"
    }
}

# Entrepreneur Dashboard Access
if ($tokens.entrepreneur) {
    $headers = @{ "Authorization" = "Bearer $($tokens.entrepreneur)" }
    $result = Test-Endpoint "$baseUrl/api/entrepreneur/dashboard" "GET" $null $headers
    if ($result.Success) {
        Add-Result "Entrepreneur Dashboard Access" "PASS" "Entrepreneur can access dashboard"
    } else {
        Add-Result "Entrepreneur Dashboard Access" "FAIL" "Entrepreneur dashboard access failed: $($result.Error)"
    }
}

# Test Unauthorized Dashboard Access
$result = Test-Endpoint "$baseUrl/api/investor/dashboard" "GET"
if (!$result.Success -and ($result.StatusCode -eq 401 -or $result.StatusCode -eq 403)) {
    Add-Result "Unauthorized Dashboard Access" "PASS" "Unauthorized dashboard access blocked"
} else {
    Add-Result "Unauthorized Dashboard Access" "FAIL" "Should block unauthorized dashboard access"
}

# ===================================================================
# 7. ADDITIONAL ENDPOINT TESTING
# ===================================================================
Write-Host "`n7. TESTING ADDITIONAL ENDPOINTS" -ForegroundColor Yellow

if ($tokens.investor) {
    $headers = @{ "Authorization" = "Bearer $($tokens.investor)" }
    
    # Investment Opportunities
    $result = Test-Endpoint "$baseUrl/api/investor/opportunities" "GET" $null $headers
    if ($result.Success -or $result.StatusCode -eq 200) {
        Add-Result "Investment Opportunities" "PASS" "Investment opportunities endpoint accessible"
    } else {
        Add-Result "Investment Opportunities" "FAIL" "Investment opportunities failed: $($result.Error)"
    }
    
    # Portfolio Management
    $result = Test-Endpoint "$baseUrl/api/investor/portfolio" "GET" $null $headers
    if ($result.Success -or $result.StatusCode -eq 200) {
        Add-Result "Portfolio Management" "PASS" "Portfolio endpoint accessible"
    } else {
        Add-Result "Portfolio Management" "FAIL" "Portfolio endpoint failed: $($result.Error)"
    }
}

if ($tokens.entrepreneur) {
    $headers = @{ "Authorization" = "Bearer $($tokens.entrepreneur)" }
    
    # Funding Opportunities
    $result = Test-Endpoint "$baseUrl/api/entrepreneur/funding" "GET" $null $headers
    if ($result.Success -or $result.StatusCode -eq 200) {
        Add-Result "Funding Opportunities" "PASS" "Funding opportunities endpoint accessible"
    } else {
        Add-Result "Funding Opportunities" "FAIL" "Funding opportunities failed: $($result.Error)"
    }
    
    # Startup Management
    $result = Test-Endpoint "$baseUrl/api/entrepreneur/startups" "GET" $null $headers
    if ($result.Success -or $result.StatusCode -eq 200) {
        Add-Result "Startup Management" "PASS" "Startup management endpoint accessible"
    } else {
        Add-Result "Startup Management" "FAIL" "Startup management failed: $($result.Error)"
    }
}

# ===================================================================
# 8. SECURITY TESTING
# ===================================================================
Write-Host "`n8. TESTING SECURITY FEATURES" -ForegroundColor Yellow

# Test Invalid JWT Token
$invalidHeaders = @{ "Authorization" = "Bearer invalid_token_here" }
$result = Test-Endpoint "$baseUrl/api/auth/profile" "GET" $null $invalidHeaders
if (!$result.Success -and ($result.StatusCode -eq 400 -or $result.StatusCode -eq 401 -or $result.StatusCode -eq 403)) {
    Add-Result "Invalid JWT Rejection" "PASS" "Invalid JWT properly rejected"
} else {
    Add-Result "Invalid JWT Rejection" "FAIL" "Should reject invalid JWT tokens"
}

# Test Malformed Authorization Header
$malformedHeaders = @{ "Authorization" = "InvalidFormat" }
$result = Test-Endpoint "$baseUrl/api/auth/profile" "GET" $null $malformedHeaders
if (!$result.Success -and ($result.StatusCode -eq 400 -or $result.StatusCode -eq 401 -or $result.StatusCode -eq 403)) {
    Add-Result "Malformed Auth Header" "PASS" "Malformed auth header rejected"
} else {
    Add-Result "Malformed Auth Header" "FAIL" "Should reject malformed auth headers"
}

# ===================================================================
# 9. PERFORMANCE & LOAD TESTING
# ===================================================================
Write-Host "`n9. TESTING PERFORMANCE" -ForegroundColor Yellow

$stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
$concurrentRequests = 5
$jobs = @()

for ($i = 1; $i -le $concurrentRequests; $i++) {
    $jobs += Start-Job -ScriptBlock {
        param($url)
        try {
            $response = Invoke-RestMethod -Uri $url -Method GET
            return @{ Success = $true; Response = $response }
        } catch {
            return @{ Success = $false; Error = $_.Exception.Message }
        }
    } -ArgumentList "$baseUrl/api/public/health"
}

$results_concurrent = $jobs | Wait-Job | Receive-Job
$stopwatch.Stop()

$successfulRequests = ($results_concurrent | Where-Object { $_.Success }).Count
if ($successfulRequests -eq $concurrentRequests) {
    Add-Result "Concurrent Requests" "PASS" "$concurrentRequests concurrent requests handled in $($stopwatch.ElapsedMilliseconds)ms"
} else {
    Add-Result "Concurrent Requests" "FAIL" "Only $successfulRequests/$concurrentRequests requests successful"
}

$jobs | Remove-Job

# ===================================================================
# FINAL RESULTS SUMMARY
# ===================================================================
Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "TEST RESULTS SUMMARY" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

$passCount = ($global:results | Where-Object { $_ -like "PASS*" }).Count
$failCount = ($global:results | Where-Object { $_ -like "FAIL*" }).Count
$totalTests = $passCount + $failCount

Write-Host "`nTotal Tests: $totalTests" -ForegroundColor White
Write-Host "Passed: $passCount" -ForegroundColor Green
Write-Host "Failed: $failCount" -ForegroundColor Red

if ($totalTests -gt 0) {
    Write-Host "Success Rate: $([math]::Round(($passCount / $totalTests) * 100, 2))%" -ForegroundColor $(if ($failCount -eq 0) { 'Green' } else { 'Yellow' })
} else {
    Write-Host "Success Rate: No tests recorded" -ForegroundColor Yellow
}

Write-Host "`nDETAILED RESULTS:" -ForegroundColor White
$global:results | ForEach-Object {
    if ($_ -like "PASS*") {
        Write-Host $_ -ForegroundColor Green
    } else {
        Write-Host $_ -ForegroundColor Red
    }
}

# ===================================================================
# TOKEN INFORMATION
# ===================================================================
Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "GENERATED TOKENS (for manual testing)" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

if ($tokens.investor) {
    Write-Host "Investor Token: $($tokens.investor)" -ForegroundColor Yellow
}
if ($tokens.entrepreneur) {
    Write-Host "Entrepreneur Token: $($tokens.entrepreneur)" -ForegroundColor Yellow
}
if ($tokens.login) {
    Write-Host "Login Token: $($tokens.login)" -ForegroundColor Yellow
}

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "API DOCUMENTATION LINKS" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Swagger UI: $baseUrl/swagger-ui/index.html" -ForegroundColor Blue
Write-Host "OpenAPI JSON: $baseUrl/v3/api-docs" -ForegroundColor Blue
Write-Host "Health Check: $baseUrl/api/public/health" -ForegroundColor Blue

Write-Host "`nTesting completed at $(Get-Date)" -ForegroundColor Gray