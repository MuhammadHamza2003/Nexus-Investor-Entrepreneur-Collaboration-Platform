Write-Host "============================================" -ForegroundColor Cyan
Write-Host "NEXUS API COMPREHENSIVE TEST SUITE - FINAL" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080"
$randomId = Get-Random -Maximum 999999
$results = @()

function Add-Result($test, $status, $details) {
    $global:results += "$status`: $test - $details"
    if ($status -eq "PASS") {
        Write-Host "[PASS] $test - $details" -ForegroundColor Green
    } else {
        Write-Host "[FAIL] $test - $details" -ForegroundColor Red
    }
}

Write-Host "`n1. TESTING PUBLIC ENDPOINTS" -ForegroundColor Yellow
try {
    $public = Invoke-RestMethod -Uri "$baseUrl/api/public/test"
    Add-Result "Public Endpoint" "PASS" $public.message
} catch {
    Add-Result "Public Endpoint" "FAIL" $_.Exception.Message
}

Write-Host "`n2. TESTING USER REGISTRATION (4 USERS)" -ForegroundColor Yellow

# User 1: Primary Investor
$investorUser1 = "testinv1_$randomId"
$investorEmail1 = "investor1_$randomId@test.com"

# User 2: Secondary Investor  
$investorUser2 = "testinv2_$randomId"
$investorEmail2 = "investor2_$randomId@test.com"

# User 3: Primary Entrepreneur
$entUser1 = "testent1_$randomId"  
$entEmail1 = "ent1_$randomId@test.com"

# User 4: Secondary Entrepreneur
$entUser2 = "testent2_$randomId"
$entEmail2 = "ent2_$randomId@test.com"

# Register Investor 1
$inv1Data = @{
    username = $investorUser1
    email = $investorEmail1
    password = "password123"
    firstName = "John"
    lastName = "Investor"
    role = "INVESTOR"
} | ConvertTo-Json

try {
    $inv1Reg = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method POST -Body $inv1Data -ContentType "application/json"
    Add-Result "Investor 1 Registration" "PASS" $inv1Reg.message
} catch {
    Add-Result "Investor 1 Registration" "FAIL" $_.Exception.Message
}

# Register Investor 2
$inv2Data = @{
    username = $investorUser2
    email = $investorEmail2
    password = "password123"
    firstName = "Sarah"
    lastName = "Capital"
    role = "INVESTOR"
} | ConvertTo-Json

try {
    $inv2Reg = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method POST -Body $inv2Data -ContentType "application/json"
    Add-Result "Investor 2 Registration" "PASS" $inv2Reg.message
} catch {
    Add-Result "Investor 2 Registration" "FAIL" $_.Exception.Message
}

# Register Entrepreneur 1
$ent1Data = @{
    username = $entUser1
    email = $entEmail1
    password = "password123"
    firstName = "Jane"
    lastName = "Entrepreneur"
    role = "ENTREPRENEUR"
} | ConvertTo-Json

try {
    $ent1Reg = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method POST -Body $ent1Data -ContentType "application/json"
    Add-Result "Entrepreneur 1 Registration" "PASS" $ent1Reg.message
} catch {
    Add-Result "Entrepreneur 1 Registration" "FAIL" $_.Exception.Message
}

# Register Entrepreneur 2
$ent2Data = @{
    username = $entUser2
    email = $entEmail2
    password = "password123"
    firstName = "Mike"
    lastName = "Startup"
    role = "ENTREPRENEUR"
} | ConvertTo-Json

try {
    $ent2Reg = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method POST -Body $ent2Data -ContentType "application/json"
    Add-Result "Entrepreneur 2 Registration" "PASS" $ent2Reg.message
} catch {
    Add-Result "Entrepreneur 2 Registration" "FAIL" $_.Exception.Message
}

Write-Host "`n3. TESTING LOGIN FOR ALL 4 USERS" -ForegroundColor Yellow

# Login Investor 1
$inv1Login = @{
    usernameOrEmail = $investorUser1
    password = "password123"
} | ConvertTo-Json

try {
    $inv1Auth = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $inv1Login -ContentType "application/json"
    Add-Result "Investor 1 Login" "PASS" "Token received - length: $($inv1Auth.token.Length)"
    $inv1Token = $inv1Auth.token
    
    # Get investor 1 profile to get user ID
    $inv1Headers = @{ "Authorization" = "Bearer $inv1Token" }
    $inv1Profile = Invoke-RestMethod -Uri "$baseUrl/api/auth/profile" -Headers $inv1Headers
    $inv1Id = $inv1Profile.id
} catch {
    Add-Result "Investor 1 Login" "FAIL" $_.Exception.Message
}

# Login Investor 2
$inv2Login = @{
    usernameOrEmail = $investorUser2
    password = "password123"
} | ConvertTo-Json

try {
    $inv2Auth = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $inv2Login -ContentType "application/json"
    Add-Result "Investor 2 Login" "PASS" "Token received - length: $($inv2Auth.token.Length)"
    $inv2Token = $inv2Auth.token
    
    # Get investor 2 profile to get user ID
    $inv2Headers = @{ "Authorization" = "Bearer $inv2Token" }
    $inv2Profile = Invoke-RestMethod -Uri "$baseUrl/api/auth/profile" -Headers $inv2Headers
    $inv2Id = $inv2Profile.id
} catch {
    Add-Result "Investor 2 Login" "FAIL" $_.Exception.Message
}

# Login Entrepreneur 1
$ent1Login = @{
    usernameOrEmail = $entUser1
    password = "password123"
} | ConvertTo-Json

try {
    $ent1Auth = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $ent1Login -ContentType "application/json"
    Add-Result "Entrepreneur 1 Login" "PASS" "Token received - length: $($ent1Auth.token.Length)"
    $ent1Token = $ent1Auth.token
    
    # Get entrepreneur 1 profile to get user ID
    $ent1Headers = @{ "Authorization" = "Bearer $ent1Token" }
    $ent1Profile = Invoke-RestMethod -Uri "$baseUrl/api/auth/profile" -Headers $ent1Headers
    $ent1Id = $ent1Profile.id
} catch {
    Add-Result "Entrepreneur 1 Login" "FAIL" $_.Exception.Message
}

# Login Entrepreneur 2
$ent2Login = @{
    usernameOrEmail = $entUser2
    password = "password123"
} | ConvertTo-Json

try {
    $ent2Auth = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $ent2Login -ContentType "application/json"
    Add-Result "Entrepreneur 2 Login" "PASS" "Token received - length: $($ent2Auth.token.Length)"
    $ent2Token = $ent2Auth.token
    
    # Get entrepreneur 2 profile to get user ID
    $ent2Headers = @{ "Authorization" = "Bearer $ent2Token" }
    $ent2Profile = Invoke-RestMethod -Uri "$baseUrl/api/auth/profile" -Headers $ent2Headers
    $ent2Id = $ent2Profile.id
} catch {
    Add-Result "Entrepreneur 2 Login" "FAIL" $_.Exception.Message
}

Write-Host "`n4. TESTING PROFILE MANAGEMENT" -ForegroundColor Yellow
if ($inv1Token) {
    try {
        $headers = @{ "Authorization" = "Bearer $inv1Token" }
        $profile = Invoke-RestMethod -Uri "$baseUrl/api/auth/profile" -Headers $headers
        Add-Result "Get Profile (Investor 1)" "PASS" "$($profile.firstName) $($profile.lastName) ($($profile.role))"
        
        # Test profile update
        $updateData = @{
            bio = "Expert investor with 10+ years experience"
            portfolio = "50+ investments in tech startups"
            preferences = "AI, FinTech, HealthTech"
        } | ConvertTo-Json
        
        $updateResult = Invoke-RestMethod -Uri "$baseUrl/api/auth/profile" -Method PUT -Headers $headers -Body $updateData -ContentType "application/json"
        Add-Result "Update Profile (Investor 1)" "PASS" "Bio and preferences updated successfully"
    } catch {
        Add-Result "Profile Management" "FAIL" $_.Exception.Message
    }
}

Write-Host "`n5. TESTING ROLE-BASED ACCESS (ALL USERS)" -ForegroundColor Yellow

# Test Investor 1 Dashboard
if ($inv1Token) {
    try {
        $headers = @{ "Authorization" = "Bearer $inv1Token" }
        $dashboard = Invoke-RestMethod -Uri "$baseUrl/api/investor/dashboard" -Headers $headers
        Add-Result "Investor 1 Dashboard" "PASS" $dashboard.message
    } catch {
        Add-Result "Investor 1 Dashboard" "FAIL" $_.Exception.Message
    }
}

# Test Investor 2 Dashboard
if ($inv2Token) {
    try {
        $headers = @{ "Authorization" = "Bearer $inv2Token" }
        $dashboard = Invoke-RestMethod -Uri "$baseUrl/api/investor/dashboard" -Headers $headers
        Add-Result "Investor 2 Dashboard" "PASS" $dashboard.message
    } catch {
        Add-Result "Investor 2 Dashboard" "FAIL" $_.Exception.Message
    }
}

# Test Entrepreneur 1 Dashboard
if ($ent1Token) {
    try {
        $headers = @{ "Authorization" = "Bearer $ent1Token" }
        $dashboard = Invoke-RestMethod -Uri "$baseUrl/api/entrepreneur/dashboard" -Headers $headers
        Add-Result "Entrepreneur 1 Dashboard" "PASS" $dashboard.message
    } catch {
        Add-Result "Entrepreneur 1 Dashboard" "FAIL" $_.Exception.Message
    }
}

# Test Entrepreneur 2 Dashboard
if ($ent2Token) {
    try {
        $headers = @{ "Authorization" = "Bearer $ent2Token" }
        $dashboard = Invoke-RestMethod -Uri "$baseUrl/api/entrepreneur/dashboard" -Headers $headers
        Add-Result "Entrepreneur 2 Dashboard" "PASS" $dashboard.message
    } catch {
        Add-Result "Entrepreneur 2 Dashboard" "FAIL" $_.Exception.Message
    }
}

Write-Host "`n6. TESTING MEETING MANAGEMENT (WITH 4 USERS)" -ForegroundColor Yellow
$meetingId = $null
$scheduledMeetingId = $null
$confirmedMeetingId = $null

if ($ent1Token -and $inv1Id -and $inv2Id) {
    try {
        $headers = @{ "Authorization" = "Bearer $ent1Token" }
        
        # Meeting 1: Main test meeting with both investors (will be cancelled at end)
        $tomorrow = (Get-Date).AddDays(1).ToString("yyyy-MM-ddTHH:mm:ss")
        $endTime = (Get-Date).AddDays(1).AddHours(1).ToString("yyyy-MM-ddTHH:mm:ss")
        
        $meetingData = @{
            title = "Investment Pitch Meeting"
            description = "Series A funding discussion with multiple investors"
            startTime = $tomorrow
            endTime = $endTime
            participantIds = @($inv1Id, $inv2Id)
            agenda = "1. Company overview 2. Financial projections 3. Funding terms 4. Q&A session"
        } | ConvertTo-Json
        
        $meeting = Invoke-RestMethod -Uri "$baseUrl/api/meetings" -Method POST -Headers $headers -Body $meetingData -ContentType "application/json"
        Add-Result "Create Multi-Investor Meeting" "PASS" "Meeting: $($meeting.title) with $($meeting.participantIds.Count) participants"
        $meetingId = $meeting.id
        
        # Meeting 2: Scheduled meeting with single investor (stays SCHEDULED)
        $dayAfterTomorrow = (Get-Date).AddDays(2).ToString("yyyy-MM-ddTHH:mm:ss")
        $dayAfterTomorrowEnd = (Get-Date).AddDays(2).AddHours(1).ToString("yyyy-MM-ddTHH:mm:ss")
        
        $scheduledMeetingData = @{
            title = "Product Demo Meeting"
            description = "Product demonstration for Investor 1"
            startTime = $dayAfterTomorrow
            endTime = $dayAfterTomorrowEnd
            participantIds = @($inv1Id)
            agenda = "1. Product walkthrough 2. Market analysis 3. Technical architecture"
        } | ConvertTo-Json
        
        $scheduledMeeting = Invoke-RestMethod -Uri "$baseUrl/api/meetings" -Method POST -Headers $headers -Body $scheduledMeetingData -ContentType "application/json"
        Add-Result "Create Scheduled Meeting" "PASS" "Scheduled meeting: $($scheduledMeeting.title) (Status: $($scheduledMeeting.status))"
        $scheduledMeetingId = $scheduledMeeting.id
        
        # Test get meetings for Entrepreneur 1
        $meetings = Invoke-RestMethod -Uri "$baseUrl/api/meetings" -Headers $headers
        Add-Result "Get Meetings (Entrepreneur 1)" "PASS" "Retrieved $($meetings.Count) meetings"
        
        # Test upcoming meetings
        $upcoming = Invoke-RestMethod -Uri "$baseUrl/api/meetings/upcoming" -Headers $headers
        Add-Result "Get Upcoming Meetings" "PASS" "Found $($upcoming.Count) upcoming meetings"
        
    } catch {
        Add-Result "Meeting Creation (Ent 1)" "FAIL" $_.Exception.Message
    }
}

# Create meeting from Entrepreneur 2 with different investors
if ($ent2Token -and $inv2Id) {
    try {
        $headers = @{ "Authorization" = "Bearer $ent2Token" }
        $threeDaysLater = (Get-Date).AddDays(3).ToString("yyyy-MM-ddTHH:mm:ss")
        $threeDaysLaterEnd = (Get-Date).AddDays(3).AddHours(1).ToString("yyyy-MM-ddTHH:mm:ss")
        
        $confirmMeetingData = @{
            title = "Due Diligence Meeting"
            description = "Financial and legal due diligence review"
            startTime = $threeDaysLater
            endTime = $threeDaysLaterEnd
            participantIds = @($inv2Id)
            agenda = "1. Financial statements review 2. Legal documents 3. Risk assessment"
        } | ConvertTo-Json
        
        $confirmMeeting = Invoke-RestMethod -Uri "$baseUrl/api/meetings" -Method POST -Headers $headers -Body $confirmMeetingData -ContentType "application/json"
        Add-Result "Create Meeting (Entrepreneur 2)" "PASS" "Meeting: $($confirmMeeting.title)"
        $confirmedMeetingId = $confirmMeeting.id
        
    } catch {
        Add-Result "Meeting Creation (Ent 2)" "FAIL" $_.Exception.Message
    }
}

# Test investors can confirm meetings
if ($confirmedMeetingId -and $inv2Token) {
    try {
        $headers = @{ "Authorization" = "Bearer $inv2Token" }
        $confirmResult = Invoke-RestMethod -Uri "$baseUrl/api/meetings/$confirmedMeetingId/confirm" -Method PUT -Headers $headers
        Add-Result "Confirm Meeting (Investor 2)" "PASS" $confirmResult.message
        
        # Verify the meeting status changed to CONFIRMED
        $confirmedMeetingDetails = Invoke-RestMethod -Uri "$baseUrl/api/meetings/$confirmedMeetingId" -Headers $headers
        Add-Result "Verify Meeting Confirmed" "PASS" "Meeting status: $($confirmedMeetingDetails.status)"
    } catch {
        Add-Result "Confirm Meeting" "FAIL" $_.Exception.Message
    }
}

Write-Host "`n7. TESTING SECURITY" -ForegroundColor Yellow

# Test unauthorized access
try {
    $unauthorized = Invoke-RestMethod -Uri "$baseUrl/api/meetings"
    Add-Result "Block Unauthorized Access" "FAIL" "Unauthorized access was allowed"
} catch {
    Add-Result "Block Unauthorized Access" "PASS" "Properly blocked unauthorized access"
}

# Test invalid token
try {
    $invalidHeaders = @{ "Authorization" = "Bearer invalid_token_123" }
    $invalidResult = Invoke-RestMethod -Uri "$baseUrl/api/meetings" -Headers $invalidHeaders
    Add-Result "Block Invalid Token" "FAIL" "Invalid token was accepted"
} catch {
    Add-Result "Block Invalid Token" "PASS" "Properly rejected invalid token"
}

# Test cross-role access with multiple users
if ($inv1Token) {
    try {
        $headers = @{ "Authorization" = "Bearer $inv1Token" }
        $crossRole = Invoke-RestMethod -Uri "$baseUrl/api/entrepreneur/dashboard" -Headers $headers
        Add-Result "Block Cross-Role Access" "FAIL" "Cross-role access was allowed"
    } catch {
        Add-Result "Block Cross-Role Access" "PASS" "Properly blocked cross-role access"
    }
}

Write-Host "`n8. CLEANUP" -ForegroundColor Yellow
if ($meetingId -and $ent1Token) {
    try {
        $headers = @{ "Authorization" = "Bearer $ent1Token" }
        $cancelResult = Invoke-RestMethod -Uri "$baseUrl/api/meetings/$meetingId" -Method DELETE -Headers $headers
        Add-Result "Cancel Multi-Investor Meeting" "PASS" $cancelResult.message
    } catch {
        Add-Result "Cancel Meeting" "FAIL" $_.Exception.Message
    }
}

# Display final meeting status summary
if ($ent1Token) {
    try {
        $headers = @{ "Authorization" = "Bearer $ent1Token" }
        $finalMeetings = Invoke-RestMethod -Uri "$baseUrl/api/meetings" -Headers $headers
        Add-Result "Final Meeting Count (Ent 1)" "PASS" "Entrepreneur 1 meetings: $($finalMeetings.Count)"
        
        Write-Host "Entrepreneur 1 meetings:" -ForegroundColor Cyan
        foreach ($meeting in $finalMeetings) {
            Write-Host "  - $($meeting.title): $($meeting.status)" -ForegroundColor Cyan
        }
    } catch {
        Add-Result "Final Meeting Check (Ent 1)" "FAIL" $_.Exception.Message
    }
}

if ($ent2Token) {
    try {
        $headers = @{ "Authorization" = "Bearer $ent2Token" }
        $finalMeetings2 = Invoke-RestMethod -Uri "$baseUrl/api/meetings" -Headers $headers
        Add-Result "Final Meeting Count (Ent 2)" "PASS" "Entrepreneur 2 meetings: $($finalMeetings2.Count)"
        
        Write-Host "Entrepreneur 2 meetings:" -ForegroundColor Cyan
        foreach ($meeting in $finalMeetings2) {
            Write-Host "  - $($meeting.title): $($meeting.status)" -ForegroundColor Cyan
        }
    } catch {
        Add-Result "Final Meeting Check (Ent 2)" "FAIL" $_.Exception.Message
    }
}

# FINAL RESULTS
Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "FINAL TEST RESULTS SUMMARY" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

$passed = ($results | Where-Object {$_ -like "PASS:*"}).Count
$failed = ($results | Where-Object {$_ -like "FAIL:*"}).Count
$total = $results.Count

Write-Host "PASSED: $passed" -ForegroundColor Green
Write-Host "FAILED: $failed" -ForegroundColor Red
Write-Host "TOTAL:  $total" -ForegroundColor Yellow

if ($total -gt 0) {
    $successRate = [math]::Round(($passed/$total)*100,1)
    Write-Host "SUCCESS RATE: $successRate%" -ForegroundColor Cyan
} else {
    Write-Host "SUCCESS RATE: 0%" -ForegroundColor Red
}

if ($failed -eq 0) {
    Write-Host "`nCONGRATULATIONS! ALL TESTS PASSED!" -ForegroundColor Green
    Write-Host "Your Nexus API is fully functional and ready for production!" -ForegroundColor Green
    Write-Host "All endpoints are working correctly with proper security!" -ForegroundColor Green
} else {
    Write-Host "`nSOME TESTS FAILED:" -ForegroundColor Yellow
    $results | Where-Object {$_ -like "FAIL:*"} | ForEach-Object {
        Write-Host "   $_" -ForegroundColor Red
    }
}

Write-Host "`nTest completed at: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
Write-Host "Nexus Investor-Entrepreneur Platform API Testing Complete!" -ForegroundColor Green
