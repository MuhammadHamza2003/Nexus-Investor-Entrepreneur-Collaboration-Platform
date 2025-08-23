# Nexus - Authentication & User Management System

## Overview

This is a Spring Boot application implementing secure JWT-based authentication with role-based access control for Investors and Entrepreneurs.

## Features Implemented

### 🔐 Authentication & Security

- **JWT-based Authentication**: Secure token-based authentication system
- **Role-based Access Control**: Two distinct roles - INVESTOR and ENTREPRENEUR
- **Password Encryption**: BCrypt password encoding for security
- **Secure Routes**: Protected endpoints with middleware security

### 📡 API Endpoints

#### Public Endpoints (No Authentication Required)

- `GET /api/public/test` - Test endpoint
- `GET /api/public/health` - Health check

#### Authentication Endpoints

- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /api/auth/profile` - Get current user profile (requires auth)
- `PUT /api/auth/profile` - Update user profile (requires auth)

#### Investor-Only Endpoints (INVESTOR role required)

- `GET /api/investor/dashboard` - Investor dashboard
- `GET /api/investor/opportunities` - Investment opportunities
- `GET /api/investor/portfolio` - Portfolio management

#### Entrepreneur-Only Endpoints (ENTREPRENEUR role required)

- `GET /api/entrepreneur/dashboard` - Entrepreneur dashboard
- `GET /api/entrepreneur/funding` - Funding opportunities
- `GET /api/entrepreneur/startups` - Startup management
- `POST /api/entrepreneur/pitch` - Submit pitch

## Technology Stack

- **Spring Boot 3.5.4**
- **Spring Security** for authentication and authorization
- **MongoDB** for data persistence
- **JWT (JSON Web Tokens)** for authentication
- **Lombok** for reducing boilerplate code
- **Maven** for dependency management

## Project Structure

```
src/
└── main/
    └── java/
        └── com/
            └── Nexus/
                └── Nexus/
                    ├── config/
                    │   └── WebSecurityConfig.java
                    ├── controller/
                    │   ├── AuthController.java
                    │   ├── InvestorController.java
                    │   ├── EntrepreneurController.java
                    │   └── PublicController.java
                    ├── dto/
                    │   ├── AuthResponse.java
                    │   ├── LoginRequest.java
                    │   ├── ProfileUpdateRequest.java
                    │   ├── RegisterRequest.java
                    │   └── UserResponse.java
                    ├── model/
                    │   ├── Role.java
                    │   └── User.java
                    ├── repository/
                    │   └── UserRepository.java
                    ├── security/
                    │   ├── AuthEntryPointJwt.java
                    │   ├── AuthTokenFilter.java
                    │   └── JwtUtils.java
                    ├── service/
                    │   ├── AuthService.java
                    │   └── UserDetailsServiceImpl.java
                    └── NexusApplication.java
```

## Configuration

### MongoDB Configuration

```properties
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=nexus
```

### JWT Configuration

```properties
nexus.app.jwtSecret=nexusSecretKeyForAuthenticationAndAuthorization2024
nexus.app.jwtExpirationMs=86400000
```

## API Usage Examples

### 1. User Registration

```bash
POST /api/auth/register
Content-Type: application/json

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

### 2. User Login

```bash
POST /api/auth/login
Content-Type: application/json

{
    "usernameOrEmail": "john_investor",
    "password": "password123"
}
```

Response:

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

### 3. Profile Management

```bash
GET /api/auth/profile
Authorization: Bearer <JWT_TOKEN>
```

```bash
PUT /api/auth/profile
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
    "bio": "Experienced investor in tech startups",
    "portfolio": "https://portfolio.example.com",
    "preferences": "AI, Fintech, Healthcare"
}
```

### 4. Role-based Access

```bash
GET /api/investor/dashboard
Authorization: Bearer <INVESTOR_JWT_TOKEN>
```

```bash
GET /api/entrepreneur/dashboard
Authorization: Bearer <ENTREPRENEUR_JWT_TOKEN>
```

## Security Features

1. **JWT Token Validation**: All protected routes validate JWT tokens
2. **Role-based Authorization**: Different access levels for different user types
3. **Password Security**: BCrypt encryption for password storage
4. **CORS Configuration**: Configurable cross-origin resource sharing
5. **Authentication Middleware**: Custom filter for JWT processing

## User Roles

### INVESTOR

- Access to investment opportunities
- Portfolio management capabilities
- Investor-specific dashboard

### ENTREPRENEUR

- Access to funding opportunities
- Startup management tools
- Pitch submission capabilities

## Running the Application

1. **Prerequisites**:

   - Java 21
   - MongoDB running on localhost:27017
   - Maven

2. **Start the application**:

   ```bash
   ./mvnw spring-boot:run
   ```

3. **Test the application**:
   ```bash
   curl http://localhost:8080/api/public/health
   ```

## Database Schema

### User Collection

```javascript
{
    "_id": ObjectId,
    "username": String (unique),
    "email": String (unique),
    "password": String (encrypted),
    "firstName": String,
    "lastName": String,
    "role": "INVESTOR" | "ENTREPRENEUR",
    "bio": String,
    "portfolio": String,
    "preferences": String,
    "profilePicture": String,
    "phoneNumber": String,
    "location": String,
    "enabled": Boolean,
    "accountNonExpired": Boolean,
    "accountNonLocked": Boolean,
    "credentialsNonExpired": Boolean,
    "createdAt": DateTime,
    "updatedAt": DateTime
}
```

## Next Steps

This implementation provides a solid foundation for authentication and user management. Future enhancements could include:

1. **Email Verification**: Implement email verification for new accounts
2. **Password Reset**: Add forgot password functionality
3. **Refresh Tokens**: Implement token refresh mechanism
4. **Rate Limiting**: Add rate limiting for API endpoints
5. **Audit Logging**: Track user activities and login attempts
6. **Two-Factor Authentication**: Add 2FA support
7. **Social Login**: Integration with Google, LinkedIn, etc.
8. **Role Permissions**: More granular permission system

## Status: ✅ COMPLETED

All milestone requirements have been successfully implemented:

- ✅ Secure JWT-based authentication
- ✅ Role-based access (Investor vs Entrepreneur)
- ✅ Register API
- ✅ Login API
- ✅ Profile management API (bio, portfolio, preferences)
- ✅ Secure routes with middleware
- ✅ Backend repo with proper folder structure
- ✅ Working authentication + profile APIs
