Write-Host "============================================" -ForegroundColor Cyan
Write-Host "NEXUS VIDEO ROOM LOAD TEST SUITE" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080"
$randomId = Get-Random -Maximum 999999
$numberOfUsers = 5  # Adjust this for load testing
$results = @()

function Add-Result($test, $status, $details, $duration = 0) {
    $global:results += @{
        Test = $test
        Status = $status
        Details = $details
        Duration = $duration
    }
    
    $durationText = if ($duration -gt 0) { " ($duration ms)" } else { "" }
    if ($status -eq "PASS") {
        Write-Host "[PASS] $test - $details$durationText" -ForegroundColor Green
    } else {
        Write-Host "[FAIL] $test - $details$durationText" -ForegroundColor Red
    }
}

Write-Host "`nTesting server connectivity..." -ForegroundColor Yellow
try {
    $public = Invoke-RestMethod -Uri "$baseUrl/api/public/test"
    Add-Result "Server Connection" "PASS" $public.message
} catch {
    Add-Result "Server Connection" "FAIL" $_.Exception.Message
    exit 1
}

Write-Host "`nCreating $numberOfUsers test users..." -ForegroundColor Yellow
$users = @()

for ($i = 1; $i -le $numberOfUsers; $i++) {
    $userName = "loadtest_${randomId}_$i"
    $userEmail = "loadtest_${randomId}_$i@test.com"
    
    $userData = @{
        username = $userName
        email = $userEmail
        password = "password123"
        firstName = "Load"
        lastName = "Test$i"
        role = if ($i -eq 1) { "ENTREPRENEUR" } else { "INVESTOR" }
    } | ConvertTo-Json
    
    try {
        $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
        $userReg = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method POST -Body $userData -ContentType "application/json"
        $stopwatch.Stop()
        
        Add-Result "User $i Registration" "PASS" $userReg.message $stopwatch.ElapsedMilliseconds
        
        # Login user immediately
        $loginData = @{
            usernameOrEmail = $userName
            password = "password123"
        } | ConvertTo-Json
        
        $stopwatch.Restart()
        $auth = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Body $loginData -ContentType "application/json"
        $stopwatch.Stop()
        
        Add-Result "User $i Login" "PASS" "Token received" $stopwatch.ElapsedMilliseconds
        
        $users += @{
            Id = $i
            Username = $userName
            Email = $userEmail
            Token = $auth.token
            Headers = @{ "Authorization" = "Bearer $($auth.token)" }
            Role = if ($i -eq 1) { "ENTREPRENEUR" } else { "INVESTOR" }
        }
    } catch {
        Add-Result "User $i Setup" "FAIL" $_.Exception.Message
    }
}

Write-Host "`nCreating test meeting..." -ForegroundColor Yellow
$hostUser = $users | Where-Object { $_.Role -eq "ENTREPRENEUR" } | Select-Object -First 1
$participantIds = ($users | Where-Object { $_.Role -eq "INVESTOR" } | ForEach-Object { $_.Id })

$meetingData = @{
    title = "Load Test Meeting $randomId"
    description = "Load testing video room with $numberOfUsers users"
    scheduledTime = (Get-Date).AddHours(1).ToString("yyyy-MM-ddTHH:mm:ss")
    duration = 60
    type = "INVESTMENT_PITCH"
    participantIds = $participantIds
} | ConvertTo-Json

try {
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    $meeting = Invoke-RestMethod -Uri "$baseUrl/api/meetings" -Method POST -Body $meetingData -ContentType "application/json" -Headers $hostUser.Headers
    $stopwatch.Stop()
    
    Add-Result "Meeting Creation" "PASS" "Meeting ID: $($meeting.id)" $stopwatch.ElapsedMilliseconds
    $meetingId = $meeting.id
} catch {
    Add-Result "Meeting Creation" "FAIL" $_.Exception.Message
    exit 1
}

Write-Host "`nTesting concurrent video room creation..." -ForegroundColor Yellow
$rooms = @()

# Test concurrent room creation
$jobs = @()
foreach ($user in $users) {
    if ($user.Role -eq "ENTREPRENEUR") {
        $videoRoomData = @{
            meetingId = $meetingId
            maxParticipants = $numberOfUsers
            enableRecording = ($user.Id % 2 -eq 0)  # Enable recording for even-numbered users
        } | ConvertTo-Json
        
        $job = Start-Job -ScriptBlock {
            param($url, $data, $headers)
            $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
            try {
                $result = Invoke-RestMethod -Uri $url -Method POST -Body $data -ContentType "application/json" -Headers $headers
                $stopwatch.Stop()
                return @{
                    Success = $true
                    Data = $result
                    Duration = $stopwatch.ElapsedMilliseconds
                }
            } catch {
                $stopwatch.Stop()
                return @{
                    Success = $false
                    Error = $_.Exception.Message
                    Duration = $stopwatch.ElapsedMilliseconds
                }
            }
        } -ArgumentList "$baseUrl/api/video/rooms", $videoRoomData, $user.Headers
        
        $jobs += @{
            Job = $job
            User = $user
        }
    }
}

# Wait for all room creation jobs to complete
foreach ($jobInfo in $jobs) {
    $result = Receive-Job -Job $jobInfo.Job -Wait
    Remove-Job -Job $jobInfo.Job
    
    if ($result.Success) {
        Add-Result "Room Creation (User $($jobInfo.User.Id))" "PASS" "Room ID: $($result.Data.roomId)" $result.Duration
        $rooms += @{
            RoomId = $result.Data.roomId
            Host = $jobInfo.User
            Data = $result.Data
        }
    } else {
        Add-Result "Room Creation (User $($jobInfo.User.Id))" "FAIL" $result.Error $result.Duration
    }
}

if ($rooms.Count -eq 0) {
    Write-Host "No rooms created successfully. Exiting..." -ForegroundColor Red
    exit 1
}

$testRoom = $rooms[0]  # Use the first successfully created room for testing

Write-Host "`nTesting concurrent room joining..." -ForegroundColor Yellow
$joinJobs = @()

foreach ($user in $users) {
    if ($user.Role -eq "INVESTOR") {
        $job = Start-Job -ScriptBlock {
            param($url, $headers)
            $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
            try {
                $result = Invoke-RestMethod -Uri $url -Method POST -Headers $headers
                $stopwatch.Stop()
                return @{
                    Success = $true
                    Data = $result
                    Duration = $stopwatch.ElapsedMilliseconds
                }
            } catch {
                $stopwatch.Stop()
                return @{
                    Success = $false
                    Error = $_.Exception.Message
                    Duration = $stopwatch.ElapsedMilliseconds
                }
            }
        } -ArgumentList "$baseUrl/api/video/rooms/$($testRoom.RoomId)/join", $user.Headers
        
        $joinJobs += @{
            Job = $job
            User = $user
        }
    }
}

# Wait for all join jobs to complete
foreach ($jobInfo in $joinJobs) {
    $result = Receive-Job -Job $jobInfo.Job -Wait
    Remove-Job -Job $jobInfo.Job
    
    if ($result.Success) {
        Add-Result "Room Join (User $($jobInfo.User.Id))" "PASS" "Joined successfully" $result.Duration
    } else {
        Add-Result "Room Join (User $($jobInfo.User.Id))" "FAIL" $result.Error $result.Duration
    }
}

Write-Host "`nTesting concurrent active room queries..." -ForegroundColor Yellow
$queryJobs = @()

foreach ($user in $users) {
    $job = Start-Job -ScriptBlock {
        param($url, $headers)
        $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
        try {
            $result = Invoke-RestMethod -Uri $url -Headers $headers
            $stopwatch.Stop()
            return @{
                Success = $true
                Data = $result
                Duration = $stopwatch.ElapsedMilliseconds
            }
        } catch {
            $stopwatch.Stop()
            return @{
                Success = $false
                Error = $_.Exception.Message
                Duration = $stopwatch.ElapsedMilliseconds
            }
        }
    } -ArgumentList "$baseUrl/api/video/rooms/active", $user.Headers
    
    $queryJobs += @{
        Job = $job
        User = $user
    }
}

# Wait for all query jobs to complete
foreach ($jobInfo in $queryJobs) {
    $result = Receive-Job -Job $jobInfo.Job -Wait
    Remove-Job -Job $jobInfo.Job
    
    if ($result.Success) {
        Add-Result "Active Rooms Query (User $($jobInfo.User.Id))" "PASS" "Found $($result.Data.Count) rooms" $result.Duration
    } else {
        Add-Result "Active Rooms Query (User $($jobInfo.User.Id))" "FAIL" $result.Error $result.Duration
    }
}

Write-Host "`nCleaning up test data..." -ForegroundColor Yellow

# End all created rooms
foreach ($room in $rooms) {
    try {
        $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
        $endResult = Invoke-RestMethod -Uri "$baseUrl/api/video/rooms/$($room.RoomId)/end" -Method POST -Headers $room.Host.Headers
        $stopwatch.Stop()
        
        Add-Result "Room Cleanup ($($room.RoomId))" "PASS" $endResult.message $stopwatch.ElapsedMilliseconds
    } catch {
        Add-Result "Room Cleanup ($($room.RoomId))" "FAIL" $_.Exception.Message
    }
}

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "LOAD TEST SUMMARY" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

$passCount = ($results | Where-Object { $_.Status -eq "PASS" }).Count
$failCount = ($results | Where-Object { $_.Status -eq "FAIL" }).Count
$totalCount = $results.Count

$avgDuration = ($results | Where-Object { $_.Duration -gt 0 } | Measure-Object -Property Duration -Average).Average

Write-Host "Total Tests: $totalCount" -ForegroundColor White
Write-Host "Passed: $passCount" -ForegroundColor Green
Write-Host "Failed: $failCount" -ForegroundColor Red
Write-Host "Success Rate: $(($passCount / $totalCount * 100).ToString('F1'))%" -ForegroundColor $(if ($failCount -eq 0) { "Green" } else { "Yellow" })
Write-Host "Average Response Time: $($avgDuration.ToString('F0')) ms" -ForegroundColor White
Write-Host "Users Tested: $numberOfUsers" -ForegroundColor White
Write-Host "Rooms Created: $($rooms.Count)" -ForegroundColor White

# Performance analysis
$performanceTests = $results | Where-Object { $_.Duration -gt 0 }
$slowTests = $performanceTests | Where-Object { $_.Duration -gt 2000 }

if ($slowTests.Count -gt 0) {
    Write-Host "`nSlow Tests (>2000ms):" -ForegroundColor Yellow
    $slowTests | ForEach-Object {
        Write-Host "  $($_.Test): $($_.Duration) ms" -ForegroundColor Yellow
    }
}

$fastTests = $performanceTests | Where-Object { $_.Duration -lt 500 }
Write-Host "`nFast Tests (<500ms): $($fastTests.Count)" -ForegroundColor Green

Write-Host "`nLoad testing completed!" -ForegroundColor Cyan
Write-Host "Test ID: $randomId" -ForegroundColor Gray
