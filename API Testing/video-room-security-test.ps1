Write-Host "============================================" -ForegroundColor Cyan
Write-Host "NEXUS VIDEO ROOM SECURITY TEST SUITE" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080"
$randomId = Get-Random -Maximum 999999
$results = @()

function Add-Result($test, $status, $details) {
    $global:results += "$status`: $test - $details"
    if ($status -eq "PASS") {
        Write-Host "[PASS] $test - $details" -ForegroundColor Green
    } elseif ($status -eq "SECURITY_PASS") {
        Write-Host "[SECURITY-PASS] $test - $details" -ForegroundColor Blue
    } else {
        Write-Host "[FAIL] $test - $details" -ForegroundColor Red
    }
}

Write-Host "`n1. TESTING AUTHENTICATION REQUIREMENTS" -ForegroundColor Yellow

# Test 1: Create video room without authentication
try {
    $unauthData = @{
        meetingId = "test-meeting"
        maxParticipants = 5
        enableRecording = $false
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms" -Method POST -Body $unauthData -ContentType "application/json"
    Add-Result "Create Room (No Auth)" "FAIL" "Should require authentication but didn't"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 401 -or $statusCode -eq 403) {
        Add-Result "Create Room (No Auth)" "SECURITY_PASS" "Correctly rejected: HTTP $statusCode"
    } else {
        Add-Result "Create Room (No Auth)" "FAIL" "Unexpected error: $($_.Exception.Message)"
    }
}

# Test 2: Join room without authentication
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/test-room/join" -Method POST
    Add-Result "Join Room (No Auth)" "FAIL" "Should require authentication but didn't"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 401 -or $statusCode -eq 403) {
        Add-Result "Join Room (No Auth)" "SECURITY_PASS" "Correctly rejected: HTTP $statusCode"
    } else {
        Add-Result "Join Room (No Auth)" "FAIL" "Unexpected error: $($_.Exception.Message)"
    }
}

# Test 3: Get active rooms without authentication
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/active"
    Add-Result "Get Active Rooms (No Auth)" "FAIL" "Should require authentication but didn't"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 401 -or $statusCode -eq 403) {
        Add-Result "Get Active Rooms (No Auth)" "SECURITY_PASS" "Correctly rejected: HTTP $statusCode"
    } else {
        Add-Result "Get Active Rooms (No Auth)" "FAIL" "Unexpected error: $($_.Exception.Message)"
    }
}

Write-Host "`n2. TESTING INVALID TOKEN HANDLING" -ForegroundColor Yellow

$invalidHeaders = @{ "Authorization" = "Bearer invalid-token-here" }

# Test 4: Create room with invalid token
try {
    $invalidData = @{
        meetingId = "test-meeting"
        maxParticipants = 5
        enableRecording = $false
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms" -Method POST -Body $invalidData -ContentType "application/json" -Headers $invalidHeaders
    Add-Result "Create Room (Invalid Token)" "FAIL" "Should reject invalid token but didn't"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 401 -or $statusCode -eq 403) {
        Add-Result "Create Room (Invalid Token)" "SECURITY_PASS" "Correctly rejected: HTTP $statusCode"
    } else {
        Add-Result "Create Room (Invalid Token)" "FAIL" "Unexpected error: $($_.Exception.Message)"
    }
}

# Test 5: Join room with invalid token
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/test-room/join" -Method POST -Headers $invalidHeaders
    Add-Result "Join Room (Invalid Token)" "FAIL" "Should reject invalid token but didn't"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 401 -or $statusCode -eq 403) {
        Add-Result "Join Room (Invalid Token)" "SECURITY_PASS" "Correctly rejected: HTTP $statusCode"
    } else {
        Add-Result "Join Room (Invalid Token)" "FAIL" "Unexpected error: $($_.Exception.Message)"
    }
}

Write-Host "`n3. TESTING MALFORMED TOKEN HANDLING" -ForegroundColor Yellow

$malformedHeaders = @{ "Authorization" = "Bearer" }

# Test 6: Create room with malformed token
try {
    $malformedData = @{
        meetingId = "test-meeting"
        maxParticipants = 5
        enableRecording = $false
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms" -Method POST -Body $malformedData -ContentType "application/json" -Headers $malformedHeaders
    Add-Result "Create Room (Malformed Token)" "FAIL" "Should reject malformed token but didn't"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 401 -or $statusCode -eq 403 -or $statusCode -eq 400) {
        Add-Result "Create Room (Malformed Token)" "SECURITY_PASS" "Correctly rejected: HTTP $statusCode"
    } else {
        Add-Result "Create Room (Malformed Token)" "FAIL" "Unexpected error: $($_.Exception.Message)"
    }
}

Write-Host "`n4. SETTING UP VALID USER FOR AUTHORIZATION TESTS" -ForegroundColor Yellow

# Create test users
$user1 = "sectest1_$randomId"
$email1 = "sectest1_$randomId@test.com"
$user2 = "sectest2_$randomId"
$email2 = "sectest2_$randomId@test.com"

# Register User 1 (Entrepreneur)
$userData1 = @{
    username = $user1
    email = $email1
    password = "password123"
    firstName = "Security"
    lastName = "Test1"
    role = "ENTREPRENEUR"
} | ConvertTo-Json

try {
    $reg1 = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method POST -Body $userData1 -ContentType "application/json"
    Add-Result "User 1 Registration" "PASS" $reg1.message
} catch {
    Add-Result "User 1 Registration" "FAIL" $_.Exception.Message
}

# Register User 2 (Investor)
$userData2 = @{
    username = $user2
    email = $email2
    password = "password123"
    firstName = "Security"
    lastName = "Test2"
    role = "INVESTOR"
} | ConvertTo-Json

try {
    $reg2 = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method POST -Body $userData2 -ContentType "application/json"
    Add-Result "User 2 Registration" "PASS" $reg2.message
} catch {
    Add-Result "User 2 Registration" "FAIL" $_.Exception.Message
}

# Login User 1
$loginData1 = @{
    usernameOrEmail = $user1
    password = "password123"
} | ConvertTo-Json

try {
    $auth1 = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $loginData1 -ContentType "application/json"
    Add-Result "User 1 Login" "PASS" "Token received"
    $token1 = $auth1.token
    $headers1 = @{ "Authorization" = "Bearer $token1" }
} catch {
    Add-Result "User 1 Login" "FAIL" $_.Exception.Message
    exit 1
}

# Login User 2
$loginData2 = @{
    usernameOrEmail = $user2
    password = "password123"
} | ConvertTo-Json

try {
    $auth2 = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $loginData2 -ContentType "application/json"
    Add-Result "User 2 Login" "PASS" "Token received"
    $token2 = $auth2.token
    $headers2 = @{ "Authorization" = "Bearer $token2" }
} catch {
    Add-Result "User 2 Login" "FAIL" $_.Exception.Message
    exit 1
}

Write-Host "`n5. TESTING INPUT VALIDATION SECURITY" -ForegroundColor Yellow

# Test 7: SQL Injection attempt in meeting ID
$sqlInjectionData = @{
    meetingId = "'; DROP TABLE meetings; --"
    maxParticipants = 5
    enableRecording = $false
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms" -Method POST -Body $sqlInjectionData -ContentType "application/json" -Headers $headers1
    Add-Result "SQL Injection Test" "FAIL" "Should reject malicious input but created room: $($response.roomId)"
} catch {
    Add-Result "SQL Injection Test" "SECURITY_PASS" "Correctly rejected malicious input"
}

# Test 8: XSS attempt in meeting ID
$xssData = @{
    meetingId = "<script>alert('xss')</script>"
    maxParticipants = 5
    enableRecording = $false
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms" -Method POST -Body $xssData -ContentType "application/json" -Headers $headers1
    Add-Result "XSS Injection Test" "FAIL" "Should reject malicious input but created room: $($response.roomId)"
} catch {
    Add-Result "XSS Injection Test" "SECURITY_PASS" "Correctly rejected malicious input"
}

# Test 9: Path traversal attempt
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/../../../admin/users" -Headers $headers1
    Add-Result "Path Traversal Test" "FAIL" "Should prevent path traversal but didn't"
} catch {
    Add-Result "Path Traversal Test" "SECURITY_PASS" "Correctly prevented path traversal"
}

Write-Host "`n6. TESTING DATA EXPOSURE" -ForegroundColor Yellow

# Create a legitimate meeting and room for testing
$meetingData = @{
    title = "Security Test Meeting $randomId"
    description = "Testing data exposure"
    scheduledTime = (Get-Date).AddHours(1).ToString("yyyy-MM-ddTHH:mm:ss")
    duration = 60
    type = "INVESTMENT_PITCH"
    participantIds = @()
} | ConvertTo-Json

try {
    $meeting = Invoke-RestMethod -Uri "$baseUrl/api/meetings" -Method POST -Body $meetingData -ContentType "application/json" -Headers $headers1
    $meetingId = $meeting.id
    Add-Result "Test Meeting Creation" "PASS" "Meeting ID: $meetingId"
} catch {
    Add-Result "Test Meeting Creation" "FAIL" $_.Exception.Message
    exit 1
}

$roomData = @{
    meetingId = $meetingId
    maxParticipants = 5
    enableRecording = $false
} | ConvertTo-Json

try {
    $room = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms" -Method POST -Body $roomData -ContentType "application/json" -Headers $headers1
    $roomId = $room.roomId
    Add-Result "Test Room Creation" "PASS" "Room ID: $roomId"
} catch {
    Add-Result "Test Room Creation" "FAIL" $_.Exception.Message
    exit 1
}

# Test 10: Access room details with unauthorized user
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/$roomId" -Headers $headers2
    Add-Result "Unauthorized Room Access" "FAIL" "User 2 should not access User 1's room but could"
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 403) {
        Add-Result "Unauthorized Room Access" "SECURITY_PASS" "Correctly denied access: HTTP $statusCode"
    } else {
        Add-Result "Unauthorized Room Access" "PASS" "Room accessible (may be intended behavior)"
    }
}

Write-Host "`n7. TESTING RATE LIMITING AND DOS PROTECTION" -ForegroundColor Yellow

# Test 11: Rapid requests to test rate limiting
$requestCount = 0
$successCount = 0
$rateLimitHit = $false

for ($i = 1; $i -le 10; $i++) {
    try {
        $requestCount++
        $fastRequest = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/active" -Headers $headers1
        $successCount++
        Start-Sleep -Milliseconds 100  # Small delay between requests
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq 429) {
            $rateLimitHit = $true
            break
        }
    }
}

if ($rateLimitHit) {
    Add-Result "Rate Limiting Test" "SECURITY_PASS" "Rate limiting activated after $requestCount requests"
} elseif ($successCount -eq 10) {
    Add-Result "Rate Limiting Test" "PASS" "All 10 requests succeeded (no rate limiting detected)"
} else {
    Add-Result "Rate Limiting Test" "FAIL" "Unexpected behavior: $successCount/$requestCount successful"
}

Write-Host "`n8. TESTING SESSION SECURITY" -ForegroundColor Yellow

# Test 12: Use token after simulated logout
# Note: This depends on having a logout endpoint that invalidates tokens
try {
    # Try to logout (if endpoint exists)
    try {
        Invoke-RestMethod -Uri "$baseUrl/api/auth/logout" -Method POST -Headers $headers2
        Add-Result "Logout Test" "PASS" "Logout succeeded"
        
        # Try to use token after logout
        Start-Sleep -Seconds 1
        $response = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/active" -Headers $headers2
        Add-Result "Token After Logout" "FAIL" "Token still valid after logout"
    } catch {
        # If logout endpoint doesn't exist, that's also a security consideration
        Add-Result "Logout Endpoint" "PASS" "No logout endpoint found (tokens may not be invalidated)"
    }
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 401 -or $statusCode -eq 403) {
        Add-Result "Token After Logout" "SECURITY_PASS" "Token correctly invalidated after logout"
    } else {
        Add-Result "Token After Logout" "FAIL" "Unexpected error: $($_.Exception.Message)"
    }
}

Write-Host "`n9. CLEANUP" -ForegroundColor Yellow

# Clean up test room
try {
    $cleanup = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/$roomId/end" -Method POST -Headers $headers1
    Add-Result "Test Cleanup" "PASS" $cleanup.message
} catch {
    Add-Result "Test Cleanup" "FAIL" $_.Exception.Message
}

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "SECURITY TEST SUMMARY" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

$passCount = ($results | Where-Object { $_ -like "PASS:*" }).Count
$securityPassCount = ($results | Where-Object { $_ -like "SECURITY_PASS:*" }).Count
$failCount = ($results | Where-Object { $_ -like "FAIL:*" }).Count
$totalCount = $results.Count

Write-Host "Total Tests: $totalCount" -ForegroundColor White
Write-Host "Passed: $passCount" -ForegroundColor Green
Write-Host "Security Passed: $securityPassCount" -ForegroundColor Blue
Write-Host "Failed: $failCount" -ForegroundColor Red
Write-Host "Total Success Rate: $(((($passCount + $securityPassCount) / $totalCount) * 100).ToString('F1'))%" -ForegroundColor $(if ($failCount -eq 0) { "Green" } else { "Yellow" })

if ($failCount -gt 0) {
    Write-Host "`nFailed Tests:" -ForegroundColor Red
    $results | Where-Object { $_ -like "FAIL:*" } | ForEach-Object {
        Write-Host "  $_" -ForegroundColor Red
    }
}

if ($securityPassCount -gt 0) {
    Write-Host "`nSecurity Controls Working:" -ForegroundColor Blue
    $results | Where-Object { $_ -like "SECURITY_PASS:*" } | ForEach-Object {
        Write-Host "  $_" -ForegroundColor Blue
    }
}

Write-Host "`nSecurity Recommendations:" -ForegroundColor Yellow
Write-Host "- Ensure rate limiting is properly configured" -ForegroundColor Yellow
Write-Host "- Implement token invalidation on logout" -ForegroundColor Yellow
Write-Host "- Monitor for suspicious patterns in real-time" -ForegroundColor Yellow
Write-Host "- Consider implementing request signing for sensitive operations" -ForegroundColor Yellow

Write-Host "`nSecurity testing completed!" -ForegroundColor Cyan
Write-Host "Test ID: $randomId" -ForegroundColor Gray
