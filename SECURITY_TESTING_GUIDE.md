# Security Testing Guide for Milestone 7

## Overview

This guide explains how to test the security enhancements implemented in Milestone 7 of the Nexus platform.

## Prerequisites

1. **MongoDB**: Ensure MongoDB is running on localhost:27017
2. **Java Application**: Start the Nexus Spring Boot application on port 8080
3. **PowerShell**: Required for running the test scripts

## Starting the Application

1. Navigate to the project directory:

   ```bash
   cd "E:\Internship\Task 2\Nexus week 3\Milestone 7"
   ```

2. Start the application:

   ```bash
   mvn spring-boot:run
   ```

   Or run the compiled JAR:

   ```bash
   java -jar target/Nexus-0.0.1-SNAPSHOT.jar
   ```

## Running Security Tests

### Automated Testing

Use the provided PowerShell script to run comprehensive security tests:

```powershell
# Run the security test script
.\API Testing\security-test.ps1

# Or specify a custom base URL
.\API Testing\security-test.ps1 -BaseUrl "http://localhost:8080"
```

### Manual Testing

#### 1. Password Validation Test

Test weak password rejection:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "weak",
    "firstName": "Test",
    "lastName": "User",
    "role": "INVESTOR"
  }'
```

Expected: HTTP 400 with validation error

#### 2. Rate Limiting Test

Make multiple rapid login attempts:

```bash
for i in {1..8}; do
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{
      "usernameOrEmail": "nonexistent@example.com",
      "password": "wrongpassword"
    }'
  echo "Attempt $i"
done
```

Expected: HTTP 429 after 5 attempts

#### 3. Input Sanitization Test

Test XSS prevention:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "SecurePass123!",
    "firstName": "<script>alert(\"xss\")</script>",
    "lastName": "User",
    "role": "INVESTOR"
  }'
```

Expected: Registration succeeds with sanitized input

#### 4. 2FA Testing

Send OTP:

```bash
curl -X POST http://localhost:8080/api/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "purpose": "LOGIN"
  }'
```

Verify OTP (use the OTP from the response):

```bash
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "otp": "123456",
    "purpose": "LOGIN"
  }'
```

## Security Features Tested

### ✅ Enhanced Password Validation

- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number
- At least one special character

### ✅ Rate Limiting

- 5 login attempts per 15 minutes per IP address
- HTTP 429 responses when exceeded
- Automatic reset after time window

### ✅ Input Validation & Sanitization

- XSS prevention (script tag removal)
- SQL injection detection
- HTML tag sanitization
- Comprehensive field validation

### ✅ Two-Factor Authentication

- 6-digit OTP generation
- 5-minute expiry time
- Maximum 3 verification attempts
- Mock email delivery for testing

### ✅ Global Exception Handling

- Structured error responses
- Validation error details
- Security-aware error messages

## Expected Test Results

When running the automated test script, you should see:

- ✅ Password validation correctly rejects weak passwords
- ✅ Input sanitization prevents XSS attacks
- ✅ Rate limiting blocks excessive login attempts
- ✅ 2FA OTP system works correctly
- ✅ Valid registrations succeed with strong passwords

## Monitoring Security Events

### Console Output

The application logs security events to the console:

- Rate limiting activations
- 2FA OTP generation (mock email content)
- Input validation failures
- Authentication attempts

### Database Monitoring

Check MongoDB collections for security data:

```javascript
// Check 2FA attempts
db.two_factor_auth.find().sort({ createdAt: -1 });

// Check user registrations
db.users.find().sort({ createdAt: -1 });
```

## Production Considerations

For production deployment:

1. **Enable Real Email**: Uncomment JavaMailSender configuration
2. **Use Redis**: Implement distributed rate limiting
3. **HTTPS Only**: Ensure all communications are encrypted
4. **Monitoring**: Add comprehensive security event logging
5. **Database Security**: Implement proper access controls

## Troubleshooting

### Common Issues

1. **Connection Refused**: Ensure the application is running on port 8080
2. **MongoDB Errors**: Verify MongoDB is running and accessible
3. **Rate Limit Not Working**: Check IP address extraction in logs
4. **OTP Not Generated**: Verify 2FA service is properly initialized

### Debug Commands

```bash
# Check application status
curl http://localhost:8080/actuator/health

# Check MongoDB connection
mongo --eval "db.adminCommand('ismaster')"

# View application logs
tail -f logs/application.log
```

## Security Testing Checklist

- [ ] Password complexity enforcement
- [ ] Rate limiting on authentication endpoints
- [ ] Input sanitization for XSS prevention
- [ ] SQL injection prevention
- [ ] 2FA OTP generation and verification
- [ ] Global exception handling
- [ ] Proper error responses (no information disclosure)
- [ ] Authentication and authorization flows
- [ ] Session management security
- [ ] CORS configuration validation

---

**Note**: This testing is designed for development and testing environments. Production testing should include additional security measures and monitoring.
