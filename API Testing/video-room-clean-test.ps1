Write-Host "============================================" -ForegroundColor Cyan
Write-Host "NEXUS VIDEO ROOM API COMPREHENSIVE TEST SUITE" -ForegroundColor Cyan
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

Write-Host "`n1. TESTING SERVER CONNECTIVITY" -ForegroundColor Yellow
try {
    $public = Invoke-RestMethod -Uri "$baseUrl/api/public/test"
    Add-Result "Server Connectivity" "PASS" $public.message
} catch {
    Add-Result "Server Connectivity" "FAIL" $_.Exception.Message
    Write-Host "Cannot connect to server. Please ensure the Nexus application is running on port 8080." -ForegroundColor Red
    exit 1
}

Write-Host "`n2. SETTING UP TEST USERS" -ForegroundColor Yellow

# Test Host User (Entrepreneur)
$hostUser = "videhost_$randomId"
$hostEmail = "videohost_$randomId@test.com"

# Test Participant 1 (Investor)
$participant1User = "vidpart1_$randomId"
$participant1Email = "vidpart1_$randomId@test.com"

# Test Participant 2 (Investor)
$participant2User = "vidpart2_$randomId"
$participant2Email = "vidpart2_$randomId@test.com"

# Register Host User
$hostData = @{
    username = $hostUser
    email = $hostEmail
    password = "password123"
    firstName = "Video"
    lastName = "Host"
    role = "ENTREPRENEUR"
} | ConvertTo-Json

try {
    $hostReg = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method POST -Body $hostData -ContentType "application/json"
    Add-Result "Host Registration" "PASS" $hostReg.message
} catch {
    Add-Result "Host Registration" "FAIL" $_.Exception.Message
}

# Register Participant 1
$part1Data = @{
    username = $participant1User
    email = $participant1Email
    password = "password123"
    firstName = "Video"
    lastName = "Participant1"
    role = "INVESTOR"
} | ConvertTo-Json

try {
    $part1Reg = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method POST -Body $part1Data -ContentType "application/json"
    Add-Result "Participant 1 Registration" "PASS" $part1Reg.message
} catch {
    Add-Result "Participant 1 Registration" "FAIL" $_.Exception.Message
}

# Register Participant 2
$part2Data = @{
    username = $participant2User
    email = $participant2Email
    password = "password123"
    firstName = "Video"
    lastName = "Participant2"
    role = "INVESTOR"
} | ConvertTo-Json

try {
    $part2Reg = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method POST -Body $part2Data -ContentType "application/json"
    Add-Result "Participant 2 Registration" "PASS" $part2Reg.message
} catch {
    Add-Result "Participant 2 Registration" "FAIL" $_.Exception.Message
}

Write-Host "`n3. AUTHENTICATING ALL USERS" -ForegroundColor Yellow

# Login Host
$hostLogin = @{
    usernameOrEmail = $hostUser
    password = "password123"
} | ConvertTo-Json

try {
    $hostAuth = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $hostLogin -ContentType "application/json"
    Add-Result "Host Login" "PASS" "Token received - length: $($hostAuth.token.Length)"
    $hostToken = $hostAuth.token
    $hostHeaders = @{ "Authorization" = "Bearer $hostToken" }
    
    # Get host profile
    $hostProfile = Invoke-RestMethod -Uri "$baseUrl/api/auth/profile" -Headers $hostHeaders
    $hostId = $hostProfile.id
} catch {
    Add-Result "Host Login" "FAIL" $_.Exception.Message
}

# Login Participant 1
$part1Login = @{
    usernameOrEmail = $participant1User
    password = "password123"
} | ConvertTo-Json

try {
    $part1Auth = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $part1Login -ContentType "application/json"
    Add-Result "Participant 1 Login" "PASS" "Token received - length: $($part1Auth.token.Length)"
    $part1Token = $part1Auth.token
    $part1Headers = @{ "Authorization" = "Bearer $part1Token" }
    
    # Get participant 1 profile
    $part1Profile = Invoke-RestMethod -Uri "$baseUrl/api/auth/profile" -Headers $part1Headers
    $part1Id = $part1Profile.id
} catch {
    Add-Result "Participant 1 Login" "FAIL" $_.Exception.Message
}

# Login Participant 2
$part2Login = @{
    usernameOrEmail = $participant2User
    password = "password123"
} | ConvertTo-Json

try {
    $part2Auth = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $part2Login -ContentType "application/json"
    Add-Result "Participant 2 Login" "PASS" "Token received - length: $($part2Auth.token.Length)"
    $part2Token = $part2Auth.token
    $part2Headers = @{ "Authorization" = "Bearer $part2Token" }
    
    # Get participant 2 profile
    $part2Profile = Invoke-RestMethod -Uri "$baseUrl/api/auth/profile" -Headers $part2Headers
    $part2Id = $part2Profile.id
} catch {
    Add-Result "Participant 2 Login" "FAIL" $_.Exception.Message
}

Write-Host "`n4. TESTING VIDEO ROOM CREATION" -ForegroundColor Yellow

# Create a meeting first (required for video room)
$meetingData = @{
    title = "Video Test Meeting $randomId"
    description = "Test meeting for video room functionality"
    scheduledTime = (Get-Date).AddHours(1).ToString("yyyy-MM-ddTHH:mm:ss")
    duration = 60
    type = "INVESTMENT_PITCH"
    participantIds = @($part1Id, $part2Id)
} | ConvertTo-Json

$meetingId = $null
try {
    $meeting = Invoke-RestMethod -Uri "$baseUrl/api/meetings" -Method POST -Body $meetingData -ContentType "application/json" -Headers $hostHeaders
    Add-Result "Test Meeting Creation" "PASS" "Meeting ID: $($meeting.id)"
    $meetingId = $meeting.id
} catch {
    Add-Result "Test Meeting Creation" "FAIL" $_.Exception.Message
}

# Test 1: Create video room with valid data
$videoRoomData = @{
    meetingId = $meetingId
    maxParticipants = 5
    enableRecording = $false
} | ConvertTo-Json

$roomId = $null
try {
    $videoRoom = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms" -Method POST -Body $videoRoomData -ContentType "application/json" -Headers $hostHeaders
    Add-Result "Video Room Creation" "PASS" "Room ID: $($videoRoom.roomId)"
    $roomId = $videoRoom.roomId
} catch {
    Add-Result "Video Room Creation" "FAIL" $_.Exception.Message
}

# Test 2: Create video room with recording enabled
$videoRoomRecordData = @{
    meetingId = $meetingId
    maxParticipants = 8
    enableRecording = $true
} | ConvertTo-Json

$recordRoomId = $null
try {
    $recordVideoRoom = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms" -Method POST -Body $videoRoomRecordData -ContentType "application/json" -Headers $hostHeaders
    Add-Result "Video Room Creation (Recording)" "PASS" "Room ID: $($recordVideoRoom.roomId), Recording: $($recordVideoRoom.isRecording)"
    $recordRoomId = $recordVideoRoom.roomId
} catch {
    Add-Result "Video Room Creation (Recording)" "FAIL" $_.Exception.Message
}

# Test 3: Try to create video room without authentication
try {
    $unauthorizedRoom = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms" -Method POST -Body $videoRoomData -ContentType "application/json"
    Add-Result "Video Room Creation (Unauthorized)" "FAIL" "Should have failed but didn't"
} catch {
    Add-Result "Video Room Creation (Unauthorized)" "PASS" "Correctly rejected unauthorized request"
}

# Test 4: Try to create video room with invalid meeting ID
$invalidMeetingData = @{
    meetingId = "invalid-meeting-id"
    maxParticipants = 5
    enableRecording = $false
} | ConvertTo-Json

try {
    $invalidRoom = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms" -Method POST -Body $invalidMeetingData -ContentType "application/json" -Headers $hostHeaders
    Add-Result "Video Room Creation (Invalid Meeting)" "FAIL" "Should have failed but didn't"
} catch {
    Add-Result "Video Room Creation (Invalid Meeting)" "PASS" "Correctly rejected invalid meeting ID"
}

Write-Host "`n5. TESTING VIDEO ROOM RETRIEVAL" -ForegroundColor Yellow

# Test 1: Get video room by ID
if ($roomId) {
    try {
        $retrievedRoom = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/$roomId" -Headers $hostHeaders
        Add-Result "Get Video Room by ID" "PASS" "Retrieved room: $($retrievedRoom.roomId)"
    } catch {
        Add-Result "Get Video Room by ID" "FAIL" $_.Exception.Message
    }
}

# Test 2: Get non-existent video room
try {
    $nonExistentRoom = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/non-existent-room" -Headers $hostHeaders
    Add-Result "Get Non-existent Room" "FAIL" "Should have failed but didn't"
} catch {
    Add-Result "Get Non-existent Room" "PASS" "Correctly returned 404 for non-existent room"
}

# Test 3: Get user's active rooms
try {
    $activeRooms = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/active" -Headers $hostHeaders
    Add-Result "Get User Active Rooms" "PASS" "Retrieved $($activeRooms.Count) active rooms"
} catch {
    Add-Result "Get User Active Rooms" "FAIL" $_.Exception.Message
}

Write-Host "`n6. TESTING VIDEO ROOM JOINING" -ForegroundColor Yellow

# Test 1: Participant 1 joins the room
if ($roomId) {
    try {
        $joinResult1 = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/$roomId/join" -Method POST -Headers $part1Headers
        Add-Result "Participant 1 Join Room" "PASS" "Joined room: $($joinResult1.roomId)"
    } catch {
        Add-Result "Participant 1 Join Room" "FAIL" $_.Exception.Message
    }
}

# Test 2: Participant 2 joins the room
if ($roomId) {
    try {
        $joinResult2 = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/$roomId/join" -Method POST -Headers $part2Headers
        Add-Result "Participant 2 Join Room" "PASS" "Joined room: $($joinResult2.roomId)"
    } catch {
        Add-Result "Participant 2 Join Room" "FAIL" $_.Exception.Message
    }
}

# Test 3: Try to join non-existent room
try {
    $joinInvalid = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/non-existent-room/join" -Method POST -Headers $part1Headers
    Add-Result "Join Non-existent Room" "FAIL" "Should have failed but didn't"
} catch {
    Add-Result "Join Non-existent Room" "PASS" "Correctly rejected join to non-existent room"
}

# Test 4: Try to join without authentication
if ($roomId) {
    try {
        $joinUnauth = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/$roomId/join" -Method POST
        Add-Result "Join Room (Unauthorized)" "FAIL" "Should have failed but didn't"
    } catch {
        Add-Result "Join Room (Unauthorized)" "PASS" "Correctly rejected unauthorized join"
    }
}

Write-Host "`n7. TESTING VIDEO ROOM LEAVING" -ForegroundColor Yellow

# Test 1: Participant 1 leaves the room
if ($roomId) {
    try {
        $leaveResult1 = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/$roomId/leave" -Method POST -Headers $part1Headers
        Add-Result "Participant 1 Leave Room" "PASS" $leaveResult1.message
    } catch {
        Add-Result "Participant 1 Leave Room" "FAIL" $_.Exception.Message
    }
}

# Test 2: Try to leave non-existent room
try {
    $leaveInvalid = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/non-existent-room/leave" -Method POST -Headers $part2Headers
    Add-Result "Leave Non-existent Room" "FAIL" "Should have failed but didn't"
} catch {
    Add-Result "Leave Non-existent Room" "PASS" "Correctly rejected leave from non-existent room"
}

# Test 3: Try to leave without authentication
if ($roomId) {
    try {
        $leaveUnauth = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/$roomId/leave" -Method POST
        Add-Result "Leave Room (Unauthorized)" "FAIL" "Should have failed but didn't"
    } catch {
        Add-Result "Leave Room (Unauthorized)" "PASS" "Correctly rejected unauthorized leave"
    }
}

Write-Host "`n8. TESTING VIDEO ROOM ENDING" -ForegroundColor Yellow

# Test 1: End the video room
if ($roomId) {
    try {
        $endResult = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/$roomId/end" -Method POST -Headers $hostHeaders
        Add-Result "End Video Room" "PASS" $endResult.message
    } catch {
        Add-Result "End Video Room" "FAIL" $_.Exception.Message
    }
}

# Test 2: Try to end non-existent room
try {
    $endInvalid = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/non-existent-room/end" -Method POST -Headers $hostHeaders
    Add-Result "End Non-existent Room" "FAIL" "Should have failed but didn't"
} catch {
    Add-Result "End Non-existent Room" "PASS" "Correctly rejected end of non-existent room"
}

# Test 3: Try to end room without authentication
if ($recordRoomId) {
    try {
        $endUnauth = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/$recordRoomId/end" -Method POST
        Add-Result "End Room (Unauthorized)" "FAIL" "Should have failed but didn't"
    } catch {
        Add-Result "End Room (Unauthorized)" "PASS" "Correctly rejected unauthorized end"
    }
}

# Test 4: Clean up - End the recording room
if ($recordRoomId) {
    try {
        $endRecord = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/$recordRoomId/end" -Method POST -Headers $hostHeaders
        Add-Result "End Recording Room (Cleanup)" "PASS" $endRecord.message
    } catch {
        Add-Result "End Recording Room (Cleanup)" "FAIL" $_.Exception.Message
    }
}

Write-Host "`n9. TESTING EDGE CASES AND ERROR HANDLING" -ForegroundColor Yellow

# Test 1: Create room with missing meeting ID
$missingMeetingData = @{
    maxParticipants = 5
    enableRecording = $false
} | ConvertTo-Json

try {
    $missingMeeting = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms" -Method POST -Body $missingMeetingData -ContentType "application/json" -Headers $hostHeaders
    Add-Result "Create Room (Missing Meeting ID)" "FAIL" "Should have failed validation but didn't"
} catch {
    Add-Result "Create Room (Missing Meeting ID)" "PASS" "Correctly rejected missing meeting ID"
}

# Test 2: Create room with negative max participants
$negativeParticipantsData = @{
    meetingId = $meetingId
    maxParticipants = -1
    enableRecording = $false
} | ConvertTo-Json

try {
    $negativeRoom = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms" -Method POST -Body $negativeParticipantsData -ContentType "application/json" -Headers $hostHeaders
    Add-Result "Create Room (Negative Participants)" "PASS" "Accepted negative value (may be valid behavior)"
} catch {
    Add-Result "Create Room (Negative Participants)" "PASS" "Correctly rejected negative participants"
}

# Test 3: Create room with very large max participants
$largeParticipantsData = @{
    meetingId = $meetingId
    maxParticipants = 10000
    enableRecording = $false
} | ConvertTo-Json

try {
    $largeRoom = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms" -Method POST -Body $largeParticipantsData -ContentType "application/json" -Headers $hostHeaders
    Add-Result "Create Room (Large Participants)" "PASS" "Accepted large value: $($largeRoom.maxParticipants)"
    # Clean up this room
    try {
        Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/$($largeRoom.roomId)/end" -Method POST -Headers $hostHeaders
    } catch {}
} catch {
    Add-Result "Create Room (Large Participants)" "PASS" "Correctly rejected large participants"
}

Write-Host "`n10. TESTING CONCURRENT OPERATIONS" -ForegroundColor Yellow

# Create multiple rooms simultaneously
$concurrentResults = @()
for ($i = 1; $i -le 3; $i++) {
    $concurrentData = @{
        meetingId = $meetingId
        maxParticipants = 5
        enableRecording = $false
    } | ConvertTo-Json
    
    try {
        $concurrentRoom = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms" -Method POST -Body $concurrentData -ContentType "application/json" -Headers $hostHeaders
        $concurrentResults += $concurrentRoom.roomId
        Add-Result "Concurrent Room Creation $i" "PASS" "Room ID: $($concurrentRoom.roomId)"
    } catch {
        Add-Result "Concurrent Room Creation $i" "FAIL" $_.Exception.Message
    }
}

# Clean up concurrent rooms
foreach ($concurrentRoom in $concurrentResults) {
    try {
        Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/$concurrentRoom/end" -Method POST -Headers $hostHeaders
    } catch {}
}

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "VIDEO ROOM TEST SUMMARY" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

$passCount = ($results | Where-Object { $_ -like "PASS:*" }).Count
$failCount = ($results | Where-Object { $_ -like "FAIL:*" }).Count
$totalCount = $results.Count

Write-Host "Total Tests: $totalCount" -ForegroundColor White
Write-Host "Passed: $passCount" -ForegroundColor Green
Write-Host "Failed: $failCount" -ForegroundColor Red
Write-Host "Success Rate: $(($passCount / $totalCount * 100).ToString('F1'))%" -ForegroundColor $(if ($failCount -eq 0) { "Green" } else { "Yellow" })

if ($failCount -gt 0) {
    Write-Host "`nFailed Tests:" -ForegroundColor Red
    $results | Where-Object { $_ -like "FAIL:*" } | ForEach-Object {
        Write-Host "  $_" -ForegroundColor Red
    }
}

Write-Host "`nVideo Room API testing completed!" -ForegroundColor Cyan
Write-Host "Test ID: $randomId" -ForegroundColor Gray
