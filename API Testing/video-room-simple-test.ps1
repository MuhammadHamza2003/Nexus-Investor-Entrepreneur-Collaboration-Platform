Write-Host "============================================" -ForegroundColor Cyan
Write-Host "NEXUS VIDEO ROOM API SIMPLE TEST SUITE" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080"
$randomId = Get-Random -Maximum 999999

function Test-Result($test, $result, $details) {
    if ($result) {
        Write-Host "[PASS] $test - $details" -ForegroundColor Green
    } else {
        Write-Host "[FAIL] $test - $details" -ForegroundColor Red
    }
}

Write-Host "`nTesting Video Room Functionality..." -ForegroundColor Yellow

# Test server connectivity
try {
    $public = Invoke-RestMethod -Uri "$baseUrl/api/public/test"
    Test-Result "Server Connection" $true $public.message
} catch {
    Test-Result "Server Connection" $false $_.Exception.Message
    Write-Host "Please ensure the Nexus application is running on port 8080." -ForegroundColor Red
    exit 1
}

# Create test user
$testUser = "videotest_$randomId"
$testEmail = "videotest_$randomId@test.com"

$userData = @{
    username = $testUser
    email = $testEmail
    password = "password123"
    firstName = "Video"
    lastName = "Tester"
    role = "ENTREPRENEUR"
} | ConvertTo-Json

try {
    $userReg = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method POST -Body $userData -ContentType "application/json"
    Test-Result "User Registration" $true $userReg.message
} catch {
    Test-Result "User Registration" $false $_.Exception.Message
}

# Login user
$loginData = @{
    usernameOrEmail = $testUser
    password = "password123"
} | ConvertTo-Json

try {
    $auth = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $loginData -ContentType "application/json"
    Test-Result "User Login" $true "Token received"
    $token = $auth.token
    $headers = @{ "Authorization" = "Bearer $token" }
} catch {
    Test-Result "User Login" $false $_.Exception.Message
    exit 1
}

# Create a test meeting
$meetingData = @{
    title = "Video Test Meeting $randomId"
    description = "Test meeting for video room"
    scheduledTime = (Get-Date).AddHours(1).ToString("yyyy-MM-ddTHH:mm:ss")
    duration = 60
    type = "INVESTMENT_PITCH"
    participantIds = @()
} | ConvertTo-Json

try {
    $meeting = Invoke-RestMethod -Uri "$baseUrl/api/meetings" -Method POST -Body $meetingData -ContentType "application/json" -Headers $headers
    Test-Result "Meeting Creation" $true "Meeting ID: $($meeting.id)"
    $meetingId = $meeting.id
} catch {
    Test-Result "Meeting Creation" $false $_.Exception.Message
    exit 1
}

# Create video room
$videoRoomData = @{
    meetingId = $meetingId
    maxParticipants = 5
    enableRecording = $false
} | ConvertTo-Json

try {
    $videoRoom = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms" -Method POST -Body $videoRoomData -ContentType "application/json" -Headers $headers
    Test-Result "Video Room Creation" $true "Room ID: $($videoRoom.roomId)"
    $roomId = $videoRoom.roomId
} catch {
    Test-Result "Video Room Creation" $false $_.Exception.Message
    exit 1
}

# Get video room details
try {
    $roomDetails = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/$roomId" -Headers $headers
    Test-Result "Get Room Details" $true "Room retrieved successfully"
} catch {
    Test-Result "Get Room Details" $false $_.Exception.Message
}

# Get user's active rooms
try {
    $activeRooms = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/active" -Headers $headers
    Test-Result "Get Active Rooms" $true "Found $($activeRooms.Count) active rooms"
} catch {
    Test-Result "Get Active Rooms" $false $_.Exception.Message
}

# Join the room (self-join for testing)
try {
    $joinResult = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/$roomId/join" -Method POST -Headers $headers
    Test-Result "Join Video Room" $true "Joined successfully"
} catch {
    Test-Result "Join Video Room" $false $_.Exception.Message
}

# Leave the room
try {
    $leaveResult = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/$roomId/leave" -Method POST -Headers $headers
    Test-Result "Leave Video Room" $true $leaveResult.message
} catch {
    Test-Result "Leave Video Room" $false $_.Exception.Message
}

# End the video room
try {
    $endResult = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/$roomId/end" -Method POST -Headers $headers
    Test-Result "End Video Room" $true $endResult.message
} catch {
    Test-Result "End Video Room" $false $_.Exception.Message
}

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "Video Room Simple Test Completed!" -ForegroundColor Cyan
Write-Host "Test ID: $randomId" -ForegroundColor Gray
Write-Host "============================================" -ForegroundColor Cyan
