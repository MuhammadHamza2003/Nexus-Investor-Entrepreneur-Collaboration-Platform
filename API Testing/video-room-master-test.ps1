Write-Host "============================================" -ForegroundColor Cyan
Write-Host "NEXUS VIDEO ROOM MASTER TEST SUITE" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Definition
$timestamp = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"
$logFile = Join-Path $scriptPath "video-room-test-results_$timestamp.log"

function Write-Log($message, $color = "White") {
    $logEntry = "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] $message"
    Write-Host $message -ForegroundColor $color
    Add-Content -Path $logFile -Value $logEntry
}

function Run-TestSuite($scriptName, $description) {
    Write-Log "`n============================================" "Cyan"
    Write-Log "RUNNING: $description" "Cyan"
    Write-Log "Script: $scriptName" "Gray"
    Write-Log "============================================" "Cyan"
    
    $scriptFile = Join-Path $scriptPath $scriptName
    
    if (Test-Path $scriptFile) {
        try {
            $startTime = Get-Date
            & $scriptFile 2>&1 | Tee-Object -FilePath $logFile -Append
            $endTime = Get-Date
            $duration = ($endTime - $startTime).TotalSeconds
            
            Write-Log "`nTest suite completed in $($duration.ToString('F1')) seconds" "Green"
            return @{
                Name = $description
                Success = $true
                Duration = $duration
                Error = $null
            }
        } catch {
            Write-Log "Error running test suite: $($_.Exception.Message)" "Red"
            return @{
                Name = $description
                Success = $false
                Duration = 0
                Error = $_.Exception.Message
            }
        }
    } else {
        Write-Log "Test script not found: $scriptFile" "Red"
        return @{
            Name = $description
            Success = $false
            Duration = 0
            Error = "Script file not found"
        }
    }
}

function Test-ServerAvailability {
    Write-Log "Checking server availability..." "Yellow"
    
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/public/test" -TimeoutSec 10
        Write-Log "Server is available: $($response.message)" "Green"
        return $true
    } catch {
        Write-Log "Server is not available: $($_.Exception.Message)" "Red"
        Write-Log "Please ensure the Nexus application is running on port 8080 before running tests." "Yellow"
        return $false
    }
}

# Initialize results
$testResults = @()
$overallStartTime = Get-Date

Write-Log "Video Room API Master Test Suite" "Cyan"
Write-Log "Started at: $($overallStartTime.ToString('yyyy-MM-dd HH:mm:ss'))" "Gray"
Write-Log "Log file: $logFile" "Gray"

# Check server availability first
if (-not (Test-ServerAvailability)) {
    Write-Log "Exiting due to server unavailability." "Red"
    exit 1
}

# Define test suites to run
$testSuites = @(
    @{
        Script = "video-room-simple-test.ps1"
        Description = "Simple Video Room Functionality Test"
        Required = $true
    },
    @{
        Script = "video-room-clean-test.ps1"
        Description = "Comprehensive Video Room API Test"
        Required = $true
    },
    @{
        Script = "video-room-security-test.ps1"
        Description = "Video Room Security & Authentication Test"
        Required = $false
    },
    @{
        Script = "video-room-load-test.ps1"
        Description = "Video Room Load & Performance Test"
        Required = $false
    }
)

# Run each test suite
foreach ($suite in $testSuites) {
    $result = Run-TestSuite -scriptName $suite.Script -description $suite.Description
    $testResults += $result
    
    # If a required test fails, ask user if they want to continue
    if ($suite.Required -and -not $result.Success) {
        Write-Log "`nRequired test suite failed!" "Red"
        $continue = Read-Host "Do you want to continue with remaining tests? (y/N)"
        if ($continue -notmatch "^[Yy]") {
            Write-Log "Stopping test execution as requested." "Yellow"
            break
        }
    }
    
    # Add delay between test suites to avoid overwhelming the server
    if ($suite -ne $testSuites[-1]) {
        Write-Log "`nWaiting 5 seconds before next test suite..." "Gray"
        Start-Sleep -Seconds 5
    }
}

$overallEndTime = Get-Date
$totalDuration = ($overallEndTime - $overallStartTime).TotalMinutes

Write-Log "`n============================================" "Cyan"
Write-Log "MASTER TEST SUITE SUMMARY" "Cyan"
Write-Log "============================================" "Cyan"

Write-Log "Execution Summary:" "White"
Write-Log "- Started: $($overallStartTime.ToString('yyyy-MM-dd HH:mm:ss'))" "Gray"
Write-Log "- Ended: $($overallEndTime.ToString('yyyy-MM-dd HH:mm:ss'))" "Gray"
Write-Log "- Total Duration: $($totalDuration.ToString('F1')) minutes" "Gray"
Write-Log "- Log File: $logFile" "Gray"

Write-Log "`nTest Suite Results:" "White"
$successfulSuites = 0
$failedSuites = 0

foreach ($result in $testResults) {
    if ($result.Success) {
        Write-Log "✓ $($result.Name) - $($result.Duration.ToString('F1'))s" "Green"
        $successfulSuites++
    } else {
        Write-Log "✗ $($result.Name) - Failed: $($result.Error)" "Red"
        $failedSuites++
    }
}

Write-Log "`nOverall Results:" "White"
Write-Log "- Test Suites Run: $($testResults.Count)" "White"
Write-Log "- Successful: $successfulSuites" "Green"
Write-Log "- Failed: $failedSuites" "Red"
Write-Log "- Success Rate: $(($successfulSuites / $testResults.Count * 100).ToString('F1'))%" $(if ($failedSuites -eq 0) { "Green" } else { "Yellow" })

if ($failedSuites -gt 0) {
    Write-Log "`nFailed Test Suites:" "Red"
    $testResults | Where-Object { -not $_.Success } | ForEach-Object {
        Write-Log "- $($_.Name): $($_.Error)" "Red"
    }
    
    Write-Log "`nTroubleshooting Tips:" "Yellow"
    Write-Log "- Ensure the Nexus application is running and accessible" "Yellow"
    Write-Log "- Check if the database is connected and operational" "Yellow"
    Write-Log "- Verify all required endpoints are implemented" "Yellow"
    Write-Log "- Check server logs for any errors during test execution" "Yellow"
}

Write-Log "`nRecommendations:" "Yellow"
Write-Log "- Review the detailed logs for each test suite" "Yellow"
Write-Log "- Run individual test suites for focused debugging" "Yellow"
Write-Log "- Monitor server performance during load tests" "Yellow"
Write-Log "- Address any security vulnerabilities found" "Yellow"

# Generate a simple HTML report
$htmlFile = Join-Path $scriptPath "video-room-test-report_$timestamp.html"
$htmlContent = @"
<!DOCTYPE html>
<html>
<head>
    <title>Nexus Video Room Test Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background-color: #f0f0f0; padding: 20px; border-radius: 5px; }
        .success { color: green; }
        .failure { color: red; }
        .summary { background-color: #f9f9f9; padding: 15px; margin: 10px 0; border-radius: 5px; }
        table { border-collapse: collapse; width: 100%; margin: 20px 0; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Nexus Video Room API Test Report</h1>
        <p>Generated on: $($overallEndTime.ToString('yyyy-MM-dd HH:mm:ss'))</p>
        <p>Total Duration: $($totalDuration.ToString('F1')) minutes</p>
    </div>
    
    <div class="summary">
        <h2>Summary</h2>
        <p>Test Suites Run: $($testResults.Count)</p>
        <p class="success">Successful: $successfulSuites</p>
        <p class="failure">Failed: $failedSuites</p>
        <p>Success Rate: $(($successfulSuites / $testResults.Count * 100).ToString('F1'))%</p>
    </div>
    
    <h2>Test Suite Details</h2>
    <table>
        <tr>
            <th>Test Suite</th>
            <th>Status</th>
            <th>Duration (seconds)</th>
            <th>Notes</th>
        </tr>
"@

foreach ($result in $testResults) {
    $status = if ($result.Success) { '<span class="success">✓ Success</span>' } else { '<span class="failure">✗ Failed</span>' }
    $notes = if ($result.Success) { "Completed successfully" } else { $result.Error }
    
    $htmlContent += @"
        <tr>
            <td>$($result.Name)</td>
            <td>$status</td>
            <td>$($result.Duration.ToString('F1'))</td>
            <td>$notes</td>
        </tr>
"@
}

$htmlContent += @"
    </table>
    
    <h2>Test Files</h2>
    <ul>
        <li><strong>Detailed Log:</strong> $logFile</li>
        <li><strong>HTML Report:</strong> $htmlFile</li>
    </ul>
    
    <p><em>For detailed test results, please refer to the log file.</em></p>
</body>
</html>
"@

try {
    $htmlContent | Out-File -FilePath $htmlFile -Encoding UTF8
    Write-Log "HTML report generated: $htmlFile" "Green"
} catch {
    Write-Log "Failed to generate HTML report: $($_.Exception.Message)" "Red"
}

Write-Log "`nMaster test suite execution completed!" "Cyan"

# Exit with appropriate code
if ($failedSuites -eq 0) {
    Write-Log "All test suites passed successfully!" "Green"
    exit 0
} else {
    Write-Log "Some test suites failed. Please review the results." "Yellow"
    exit 1
}
