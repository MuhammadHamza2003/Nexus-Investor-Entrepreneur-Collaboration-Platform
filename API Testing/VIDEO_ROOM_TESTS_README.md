# Nexus API Comprehensive Test Suite

This directory contains comprehensive test suites for all Nexus platform functionality including Authentication, Meetings, Video Rooms, Document Processing, and E-Signature systems. The tests validate the complete API ecosystem and ensure production readiness.

## 🧪 Complete Test Files Overview

### MASTER TEST SUITE

#### 1. clean-test.ps1 - **MAIN COMPREHENSIVE TEST** ⭐

**Purpose**: Complete platform functionality validation including all new document processing features
**Features**:

- ✅ Multi-user authentication (4 users: 2 investors, 2 entrepreneurs)
- ✅ Profile management and role-based access control
- ✅ Meeting management with conflict detection
- ✅ Meeting search and filtering
- ✅ Document upload and management testing
- ✅ E-signature system validation
- ✅ Document preview and download
- ✅ Meeting-document integration
- ✅ Video room creation and management
- ✅ Security and access control validation
- ✅ Complete cleanup and verification

**Use Case**: Primary test suite for full platform validation and CI/CD pipelines

#### 2. document-processing-comprehensive-test.ps1 - **DOCUMENT SYSTEM TEST** 📄

**Purpose**: Comprehensive testing of Milestone 5 Document Processing Chamber
**Features**:

- ✅ Document Upload & Management APIs
  - `POST /api/v1/documents/upload`
  - `GET /api/v1/documents/{id}`
  - `GET /api/v1/documents/my-documents`
  - `DELETE /api/documents/{id}`
- ✅ E-Signature System APIs
  - `POST /api/v1/documents/{id}/sign`
  - `GET /api/v1/documents/requiring-signature`
- ✅ Document Preview & Download APIs
  - `GET /api/documents/{id}/preview`
  - `GET /api/documents/download/{path}`
  - `GET /api/documents/{id}/metadata`
- ✅ Meeting Integration APIs
  - `GET /api/documents/meeting/{meetingId}`
- ✅ Document security and validation
- ✅ File type and size validation
- ✅ Document lifecycle management
- ✅ Multi-format document support
- ✅ Cloud storage integration testing

**Use Case**: Specialized testing for document processing and e-signature functionality

#### 3. video-room-comprehensive-test.ps1 - **VIDEO SYSTEM TEST** 🎥

**Purpose**: Complete Video Calling Backend Testing (Milestone 4)
**Features**:

- ✅ Multi-user video room testing (host + 3 participants)
- ✅ Video room creation with meeting integration
- ✅ Participant join/leave operations
- ✅ WebRTC signaling preparation
- ✅ Socket.IO server connectivity testing
- ✅ Room capacity and limits validation
- ✅ Recording functionality testing
- ✅ Room security and access control
- ✅ Real-time participant tracking
- ✅ Room lifecycle management

**Use Case**: Specialized testing for video calling and WebRTC functionality

### LEGACY TEST SUITES (Video Room Specific)

#### 4. video-room-simple-test.ps1

**Purpose**: Quick validation of basic video room functionality
**Features**:

- Server connectivity check
- User registration and authentication
- Meeting creation
- Video room creation, retrieval, and management
- Basic join/leave operations
- Cleanup

**Use Case**: Quick smoke test to verify basic functionality is working

### 2. video-room-clean-test.ps1

**Purpose**: Comprehensive testing of all video room API endpoints
**Features**:

- Multi-user testing (host + 2 participants)
- Complete video room lifecycle testing
- Error handling validation
- Edge case testing
- Concurrent operations testing
- Detailed reporting

**Use Case**: Full regression testing of video room functionality

### 3. video-room-security-test.ps1

**Purpose**: Security and authentication testing
**Features**:

- Authentication requirement validation
- Invalid/malformed token handling
- Input validation (SQL injection, XSS prevention)
- Authorization testing
- Rate limiting detection
- Session security testing

**Use Case**: Security auditing and penetration testing

### 4. video-room-load-test.ps1

**Purpose**: Performance and scalability testing
**Features**:

- Concurrent user simulation
- Performance measurement
- Load testing with configurable user count
- Response time analysis
- Resource usage monitoring

**Use Case**: Performance benchmarking and scalability validation

### 5. video-room-master-test.ps1

**Purpose**: Orchestrates all test suites with comprehensive reporting
**Features**:

- Runs all test suites in sequence
- Generates detailed logs
- Creates HTML reports
- Provides summary statistics
- Error handling and recovery

**Use Case**: Complete test automation for CI/CD pipelines

## API Endpoints Tested

| Endpoint                          | Method | Description             | Tests                                      |
| --------------------------------- | ------ | ----------------------- | ------------------------------------------ |
| `/api/video/rooms`                | POST   | Create video room       | ✓ Creation, ✓ Authentication, ✓ Validation |
| `/api/video/rooms/{roomId}`       | GET    | Get video room details  | ✓ Retrieval, ✓ Authorization               |
| `/api/video/rooms/active`         | GET    | Get user's active rooms | ✓ Listing, ✓ Authentication                |
| `/api/video/rooms/{roomId}/join`  | POST   | Join video room         | ✓ Joining, ✓ Authorization                 |
| `/api/video/rooms/{roomId}/leave` | POST   | Leave video room        | ✓ Leaving, ✓ Cleanup                       |
| `/api/video/rooms/{roomId}/end`   | POST   | End video room          | ✓ Termination, ✓ Authorization             |

## Prerequisites

1. **Nexus Application**: Ensure the Nexus application is running on `http://localhost:8080`
2. **Database**: MongoDB should be running and accessible
3. **PowerShell**: Windows PowerShell 5.1 or PowerShell Core 6+
4. **Network**: No firewall blocking local connections

## Running the Tests

### Quick Test

```powershell
.\video-room-simple-test.ps1
```

### Full Test Suite

```powershell
.\video-room-master-test.ps1
```

### Individual Test Suites

```powershell
# Comprehensive functionality test
.\video-room-clean-test.ps1

# Security testing
.\video-room-security-test.ps1

# Performance testing
.\video-room-load-test.ps1
```

## Test Configuration

### Load Test Configuration

Edit `video-room-load-test.ps1` to adjust:

```powershell
$numberOfUsers = 5  # Change this value for different load levels
```

### Base URL Configuration

All test files can be configured to test different environments:

```powershell
$baseUrl = "http://localhost:8080"  # Change for different environments
```

## Test Results

### Console Output

All tests provide real-time colored console output:

- 🟢 **Green**: Successful tests
- 🔴 **Red**: Failed tests
- 🔵 **Blue**: Security controls working correctly
- 🟡 **Yellow**: Warnings or informational messages

### Log Files

The master test suite generates:

- **Detailed Log**: `video-room-test-results_YYYY-MM-DD_HH-mm-ss.log`
- **HTML Report**: `video-room-test-report_YYYY-MM-DD_HH-mm-ss.html`

### Test Metrics

Each test suite reports:

- Total tests executed
- Pass/fail counts
- Success rates
- Performance metrics (where applicable)
- Security findings

## Understanding Test Results

### Success Criteria

- **PASS**: Test executed successfully and met expectations
- **SECURITY_PASS**: Security control is working correctly (blocks unauthorized access)
- **FAIL**: Test failed or didn't meet expectations

### Common Issues

#### Server Not Running

```
[FAIL] Server Connection - Cannot connect to server
```

**Solution**: Start the Nexus application on port 8080

#### Database Issues

```
[FAIL] User Registration - Database connection error
```

**Solution**: Ensure MongoDB is running and accessible

#### Authentication Problems

```
[FAIL] User Login - Invalid credentials
```

**Solution**: Check if user registration is working correctly

#### Port Conflicts

```
[FAIL] Video Room Creation - Port already in use
```

**Solution**: Ensure no other services are using required ports

## Test Data Cleanup

All test suites include automatic cleanup:

- Created users are temporary (with random IDs)
- Video rooms are ended after testing
- Meetings are created and cleaned up
- No persistent test data remains

## Security Testing Notes

The security test suite validates:

- **Authentication**: All endpoints require valid tokens
- **Authorization**: Users can only access appropriate resources
- **Input Validation**: Malicious inputs are rejected
- **Rate Limiting**: Excessive requests are throttled
- **Session Security**: Tokens are properly managed

## Performance Testing Notes

The load test suite measures:

- **Response Times**: API endpoint performance
- **Concurrency**: Multiple users performing actions simultaneously
- **Throughput**: Number of operations per second
- **Resource Usage**: Server performance under load

## Contributing

When adding new tests:

1. Follow the existing naming convention
2. Include proper error handling
3. Add cleanup procedures
4. Document the test purpose and scope
5. Update this README with new test information

## Troubleshooting

### Test Execution Issues

1. Check PowerShell execution policy: `Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser`
2. Ensure all scripts are in the same directory
3. Verify server accessibility manually

### Performance Issues

1. Reduce the number of concurrent users in load tests
2. Check server resources (CPU, memory)
3. Monitor database performance
4. Review network connectivity

### Security Test Failures

1. Review application security configuration
2. Check authentication middleware
3. Verify input validation rules
4. Monitor security logs

## API Documentation

For detailed API documentation, refer to:

- `VIDEO_CALLING_API.md` in the project root
- OpenAPI/Swagger documentation (if available)
- Source code documentation in controller classes

## Support

For issues or questions:

1. Check the test logs for detailed error information
2. Review the application logs for server-side errors
3. Consult the main project documentation
4. Create an issue in the project repository
