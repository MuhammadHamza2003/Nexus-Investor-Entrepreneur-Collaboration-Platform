# Nexus API Testing Guide - Milestone 8

This document provides comprehensive testing instructions for the Nexus Investor-Entrepreneur Collaboration Platform APIs.

## Overview

The Nexus API provides secure JWT-based authentication with role-based access control for investors and entrepreneurs. This testing suite validates all authentication flows, profile management, and role-based security features.

## Test Environment Setup

### Prerequisites

- Application running on `http://localhost:8080`
- MongoDB running on `localhost:27017`
- PowerShell 5.1+ or Postman for running tests

### Quick Start

1. Start the Nexus application:

   ```bash
   mvn spring-boot:run
   ```

2. Run comprehensive tests:

   ```powershell
   .\API Testing\milestone-8-comprehensive-test.ps1
   ```

3. Import Postman collection:
   - Import `API Testing/Nexus-API-Final-Collection.postman_collection.json`
   - Set base_url variable to `http://localhost:8080`

## API Endpoints Overview

### Public Endpoints (No Authentication)

- `GET /api/public/health` - Health check endpoint
- `GET /api/public/test` - Public test endpoint
- `GET /swagger-ui/index.html` - Swagger UI documentation
- `GET /v3/api-docs` - OpenAPI specification

### Authentication Endpoints

- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User authentication
- `GET /api/auth/profile` - Get user profile (requires auth)
- `PUT /api/auth/profile` - Update user profile (requires auth)

### Role-Based Endpoints

#### Investor Only (Requires INVESTOR role)

- `GET /api/investor/dashboard` - Investor dashboard
- `GET /api/investor/opportunities` - Investment opportunities
- `GET /api/investor/portfolio` - Portfolio management

#### Entrepreneur Only (Requires ENTREPRENEUR role)

- `GET /api/entrepreneur/dashboard` - Entrepreneur dashboard
- `GET /api/entrepreneur/funding` - Funding opportunities
- `GET /api/entrepreneur/startups` - Startup management
- `POST /api/entrepreneur/pitch` - Submit pitch

## Test Scenarios

### 1. Registration Testing

#### Valid Registration - Investor

```json
POST /api/auth/register
{
    "username": "john_investor",
    "email": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "role": "INVESTOR",
    "phoneNumber": "+1234567890",
    "location": "New York, USA"
}
```

**Expected Response (200):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "john_investor",
  "email": "john@example.com",
  "role": "INVESTOR",
  "message": "User registered successfully!"
}
```

#### Valid Registration - Entrepreneur

```json
POST /api/auth/register
{
    "username": "jane_entrepreneur",
    "email": "jane@example.com",
    "password": "password123",
    "firstName": "Jane",
    "lastName": "Smith",
    "role": "ENTREPRENEUR",
    "phoneNumber": "+1234567891",
    "location": "San Francisco, USA"
}
```

#### Invalid Registration Scenarios

- Duplicate username/email (Expected: 400)
- Missing required fields (Expected: 400)
- Invalid email format (Expected: 400)
- Weak password (Expected: 400)

### 2. Authentication Testing

#### Valid Login

```json
POST /api/auth/login
{
    "usernameOrEmail": "john_investor",
    "password": "password123"
}
```

**Expected Response (200):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "john_investor",
  "email": "john@example.com",
  "role": "INVESTOR",
  "message": "User logged in successfully!"
}
```

#### Invalid Login Scenarios

- Wrong password (Expected: 400)
- Non-existent user (Expected: 400)
- Missing credentials (Expected: 400)

### 3. Profile Management Testing

#### Get Profile

```http
GET /api/auth/profile
Authorization: Bearer {jwt_token}
```

**Expected Response (200):**

```json
{
  "id": "user_id_here",
  "username": "john_investor",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "INVESTOR",
  "phoneNumber": "+1234567890",
  "location": "New York, USA",
  "bio": null,
  "portfolio": null,
  "preferences": null,
  "createdAt": "2024-01-01T00:00:00Z"
}
```

#### Update Profile

```http
PUT /api/auth/profile
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
    "bio": "Experienced investor in tech startups",
    "portfolio": "https://portfolio.example.com",
    "preferences": "AI, Fintech, Healthcare",
    "location": "Updated Location"
}
```

### 4. Role-Based Access Control Testing

#### Investor Dashboard Access

```http
GET /api/investor/dashboard
Authorization: Bearer {investor_jwt_token}
```

**Expected for INVESTOR (200):**

```json
{
    "message": "Welcome to Investor Dashboard, John!",
    "user": {...},
    "description": "You have access to investment opportunities and portfolio management."
}
```

**Expected for ENTREPRENEUR (403):**

```json
{
  "error": "Forbidden",
  "message": "Access denied"
}
```

#### Cross-Role Access Testing

- Investor trying to access entrepreneur endpoints (Expected: 403)
- Entrepreneur trying to access investor endpoints (Expected: 403)
- Unauthenticated access to protected endpoints (Expected: 401)

### 5. Security Testing

#### Invalid JWT Token

```http
GET /api/auth/profile
Authorization: Bearer invalid_token_here
```

Expected: 401 Unauthorized

#### Malformed Authorization Header

```http
GET /api/auth/profile
Authorization: InvalidFormat
```

Expected: 401 Unauthorized

#### Missing Authorization Header

```http
GET /api/auth/profile
```

Expected: 401 Unauthorized

## Automated Testing

### PowerShell Test Script

The comprehensive test script `milestone-8-comprehensive-test.ps1` includes:

- ✅ Public endpoint availability
- ✅ API documentation accessibility (Swagger)
- ✅ User registration (both roles)
- ✅ User authentication
- ✅ Profile management (get/update)
- ✅ Role-based access control
- ✅ Security validation
- ✅ Performance testing (concurrent requests)
- ✅ Error handling validation

### Test Execution

```powershell
# Run from project root directory
.\API Testing\milestone-8-comprehensive-test.ps1
```

### Expected Output

```
============================================
NEXUS API COMPREHENSIVE TEST SUITE - FINAL
============================================

1. TESTING PUBLIC ENDPOINTS
[PASS] Health Check - Service is healthy: Application is running
[PASS] Public Test - Public endpoint accessible: Public API is working!

2. TESTING API DOCUMENTATION
[PASS] Swagger UI - Swagger UI is accessible
[PASS] OpenAPI Docs - OpenAPI specification available

... (additional test results)

============================================
TEST RESULTS SUMMARY
============================================

Total Tests: 25
Passed: 25
Failed: 0
Success Rate: 100%
```

### Postman Collection

The Postman collection `Nexus-API-Final-Collection.postman_collection.json` includes:

- **Public Endpoints** - Health check and test endpoints
- **Authentication** - Registration and login flows
- **Profile Management** - Get and update profile
- **Role-Based Access** - Dashboard and feature access
- **API Documentation** - Swagger UI and OpenAPI docs

#### Collection Variables

- `base_url`: http://localhost:8080 (local) or deployment URL
- `token`: Auto-populated from authentication responses
- `user_id`: Auto-populated from registration

#### Test Automation

Each request includes pre-request scripts and test assertions:

- Status code validation
- Response structure validation
- Token extraction and storage
- Role-based access validation

## Performance Benchmarks

### Expected Response Times

- Public endpoints: < 100ms
- Authentication: < 200ms
- Profile operations: < 150ms
- Dashboard access: < 200ms

### Concurrent Load Testing

The test script validates handling of 5 concurrent requests to ensure basic scalability.

## Troubleshooting

### Common Issues

1. **Application Not Running**

   ```
   Error: Unable to connect to localhost:8080
   Solution: Start the application with `mvn spring-boot:run`
   ```

2. **MongoDB Connection Issues**

   ```
   Error: MongoDB connection failed
   Solution: Ensure MongoDB is running on localhost:27017
   ```

3. **JWT Token Expired**

   ```
   Error: 401 Unauthorized
   Solution: Re-authenticate to get a new token
   ```

4. **CORS Issues**
   ```
   Error: CORS policy blocked request
   Solution: Check CORS configuration in WebSecurityConfig
   ```

### Debug Mode

Add debug headers to requests for detailed error information:

```http
X-Debug: true
```

## API Documentation

### Swagger UI

Access interactive API documentation at: `http://localhost:8080/swagger-ui/index.html`

Features:

- Interactive API testing
- Request/response examples
- Schema definitions
- Authentication flow
- Real-time API exploration

### OpenAPI Specification

Download the complete API specification: `http://localhost:8080/v3/api-docs`

## Integration Examples

### Frontend Integration

```javascript
// Authentication
const response = await fetch("/api/auth/login", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    usernameOrEmail: "user@example.com",
    password: "password123",
  }),
});

const { token } = await response.json();

// Authenticated requests
const profile = await fetch("/api/auth/profile", {
  headers: { Authorization: `Bearer ${token}` },
});
```

### Mobile App Integration

```swift
// iOS Swift example
struct AuthService {
    func login(username: String, password: String) async throws -> AuthResponse {
        let url = URL(string: "http://localhost:8080/api/auth/login")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        let body = ["usernameOrEmail": username, "password": password]
        request.httpBody = try JSONSerialization.data(withJSONObject: body)

        let (data, _) = try await URLSession.shared.data(for: request)
        return try JSONDecoder().decode(AuthResponse.self, from: data)
    }
}
```

## Deployment Testing

When testing against deployed instances, update the base URL:

### Production Testing

```powershell
# Update base URL in test script
$baseUrl = "https://nexus-api.onrender.com"
```

### Staging Testing

```powershell
# Update base URL for staging
$baseUrl = "https://nexus-staging.herokuapp.com"
```

## Security Considerations

### API Security Features Tested

- JWT token validation
- Role-based access control (RBAC)
- Password encryption (BCrypt)
- CORS protection
- Request rate limiting
- Input validation and sanitization

### Security Best Practices

- Always use HTTPS in production
- Implement proper token expiration
- Use secure password policies
- Enable request rate limiting
- Validate all input data
- Log security events

## Conclusion

This comprehensive testing suite ensures the Nexus API meets all security, functionality, and performance requirements for Milestone 8. The combination of automated PowerShell scripts and Postman collections provides thorough validation of all authentication and user management features.
