# Nexus - Investor-Entrepreneur Collaboration Platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-green.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![MongoDB](https://img.shields.io/badge/MongoDB-Latest-green.svg)](https://www.mongodb.com/)
[![JWT](https://img.shields.io/badge/JWT-Authentication-blue.svg)](https://jwt.io/)
[![Maven](https://img.shields.io/badge/Maven-4.0.0-blue.svg)](https://maven.apache.org/)

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Security Features](#security-features)
- [Database Schema](#database-schema)
- [Configuration](#configuration)
- [Testing](#testing)
- [Deployment](#deployment)
- [Contributing](#contributing)

## 🎯 Overview

Nexus is a comprehensive Spring Boot application designed to facilitate collaboration between investors and entrepreneurs. The platform provides secure JWT-based authentication, role-based access control, and sophisticated meeting scheduling capabilities to streamline business interactions.

### Key Objectives

- **Connect** investors with promising entrepreneurs
- **Facilitate** secure and organized meetings
- **Manage** investment opportunities and pitches
- **Provide** role-specific dashboards and functionalities

## ✨ Features

### 🔐 Authentication & Security

- **JWT-based Authentication**: Stateless, secure token-based authentication
- **Role-based Access Control**: Granular permissions for INVESTOR and ENTREPRENEUR roles
- **Password Security**: BCrypt encryption with salt for secure password storage
- **Protected Routes**: Middleware-based route protection with JWT validation
- **CORS Configuration**: Configurable cross-origin resource sharing
- **Security Headers**: Comprehensive security header configuration

### 📅 Meeting Scheduling System

- **Smart Scheduling**: Intuitive meeting creation with conflict detection
- **Multi-participant Support**: Schedule meetings with multiple stakeholders
- **Conflict Prevention**: Automatic detection and prevention of overlapping meetings
- **Meeting Lifecycle**: Complete status tracking (SCHEDULED → CONFIRMED → IN_PROGRESS → COMPLETED)
- **Real-time Updates**: Meeting modifications with participant notifications
- **Flexible Management**: Update, cancel, reschedule, and confirm meetings

### 👥 User Management

- **Dual Role System**: Separate interfaces for investors and entrepreneurs
- **Profile Management**: Comprehensive user profiles with bio, portfolio, and preferences
- **Account Security**: Secure registration and login with email verification ready
- **User Dashboard**: Role-specific dashboards with relevant information

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
- `GET /api/investor/meetings/entrepreneurs` - Meetings with entrepreneurs
- `GET /api/investor/scheduled-meetings` - Scheduled meetings

#### Entrepreneur-Only Endpoints (ENTREPRENEUR role required)

- `GET /api/entrepreneur/dashboard` - Entrepreneur dashboard
- `GET /api/entrepreneur/funding` - Funding opportunities
- `GET /api/entrepreneur/startups` - Startup management
- `POST /api/entrepreneur/pitch` - Submit pitch
- `GET /api/entrepreneur/meetings/investors` - Meetings with investors
- `GET /api/entrepreneur/scheduled-meetings` - Scheduled meetings

#### Meeting Management Endpoints (Authentication required)

- `POST /api/meetings` - Create a new meeting
- `GET /api/meetings` - Get all meetings for current user
- `GET /api/meetings/upcoming` - Get upcoming meetings
- `GET /api/meetings/{id}` - Get specific meeting details
- `PUT /api/meetings/{id}` - Update meeting (organizer only)
- `DELETE /api/meetings/{id}` - Cancel meeting (organizer only)
- `PUT /api/meetings/{id}/confirm` - Confirm meeting attendance

#### Test Endpoints (Development/Debug)

- `GET /api/test/status` - Service status and available endpoints
- `POST /api/test/echo` - Echo request payload for testing

## 🛠️ Technology Stack

- **Spring Boot 3.5.4** - Main framework
- **Spring Security 6.x** - Authentication and authorization
- **MongoDB** - NoSQL database for data persistence
- **JWT (JSON Web Tokens)** - Stateless authentication
- **Lombok** - Reducing boilerplate code
- **Maven** - Dependency management and build automation
- **Java 21** - Programming language
- **Jackson** - JSON processing
- **Jakarta Validation** - Bean validation

## 📁 Project Structure

```
src/
└── main/
    ├── java/
    │   └── com/
    │       └── Nexus/
    │           └── Nexus/
    │               ├── NexusApplication.java           # Main application class
    │               ├── config/
    │               │   └── WebSecurityConfig.java      # Security configuration
    │               ├── controller/
    │               │   ├── AuthController.java         # Authentication endpoints
    │               │   ├── InvestorController.java     # Investor-specific endpoints
    │               │   ├── EntrepreneurController.java # Entrepreneur-specific endpoints
    │               │   ├── MeetingController.java      # Meeting management endpoints
    │               │   ├── PublicController.java       # Public endpoints
    │               │   └── TestController.java         # Test/debug endpoints
    │               ├── dto/
    │               │   ├── AuthResponse.java           # Authentication response
    │               │   ├── LoginRequest.java           # Login request payload
    │               │   ├── MeetingCreateRequest.java   # Meeting creation payload
    │               │   ├── MeetingResponse.java        # Meeting response data
    │               │   ├── MeetingUpdateRequest.java   # Meeting update payload
    │               │   ├── ProfileUpdateRequest.java   # Profile update payload
    │               │   ├── RegisterRequest.java        # Registration request payload
    │               │   └── UserResponse.java           # User data response
    │               ├── model/
    │               │   ├── User.java                   # User entity
    │               │   ├── Meeting.java                # Meeting entity
    │               │   ├── Role.java                   # User roles enum
    │               │   └── MeetingStatus.java          # Meeting status enum
    │               ├── repository/
    │               │   ├── UserRepository.java         # User data access
    │               │   └── MeetingRepository.java      # Meeting data access
    │               ├── security/
    │               │   ├── AuthEntryPointJwt.java      # JWT authentication entry point
    │               │   ├── AuthTokenFilter.java        # JWT token filter
    │               │   └── JwtUtils.java               # JWT utility methods
    │               └── service/
    │                   ├── AuthService.java            # Authentication business logic
    │                   ├── MeetingService.java         # Meeting business logic
    │                   └── UserDetailsServiceImpl.java # Spring Security user details
    └── resources/
        ├── application.properties                      # Application configuration
        ├── static/                                     # Static web resources (if any)
        └── templates/                                  # View templates (if any)
```

## ⚙️ Configuration

### MongoDB Configuration

```properties
# Database Configuration
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=nexus
```

### JWT Configuration

```properties
# JWT Security Configuration
nexus.app.jwtSecret=nexusSecretKeyForAuthenticationAndAuthorization2024
nexus.app.jwtExpirationMs=86400000
# Token expires in 24 hours (86400000 milliseconds)
```

### Application Properties

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=nexus

# JWT Configuration
nexus.app.jwtSecret=nexusSecretKeyForAuthenticationAndAuthorization2024
nexus.app.jwtExpirationMs=86400000

# Logging Configuration
logging.level.com.Nexus.Nexus=DEBUG
logging.level.org.springframework.security=DEBUG
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

### 5. Meeting Management

#### Create a Meeting

```bash
POST /api/meetings
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
    "title": "Investment Discussion",
    "description": "Discussing Series A funding opportunity",
    "startTime": "2025-08-25T10:00:00",
    "endTime": "2025-08-25T11:00:00",
    "participantIds": ["64f123abc456def789", "64f456def789abc123"],
    "agenda": "1. Company overview 2. Financial projections 3. Market analysis",
    "meetingLink": "https://zoom.us/j/123456789",
    "location": "Conference Room A"
}
```

Response:

```json
{
  "id": "64f789def123abc456",
  "title": "Investment Discussion",
  "description": "Discussing Series A funding opportunity",
  "startTime": "2025-08-25T10:00:00",
  "endTime": "2025-08-25T11:00:00",
  "organizerId": "64f123abc456def789",
  "organizerName": "John Investor",
  "participantIds": ["64f123abc456def789", "64f456def789abc123"],
  "participantNames": ["John Investor", "Jane Entrepreneur"],
  "status": "SCHEDULED",
  "meetingLink": "https://zoom.us/j/123456789",
  "location": "Conference Room A",
  "agenda": "1. Company overview 2. Financial projections 3. Market analysis",
  "createdAt": "2025-08-23T14:30:00",
  "updatedAt": "2025-08-23T14:30:00"
}
```

#### Get Upcoming Meetings

```bash
GET /api/meetings/upcoming
Authorization: Bearer <JWT_TOKEN>
```

#### Update a Meeting

```bash
PUT /api/meetings/64f789def123abc456
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
    "startTime": "2025-08-25T14:00:00",
    "endTime": "2025-08-25T15:00:00",
    "notes": "Rescheduled due to client availability"
}
```

#### Confirm Meeting Attendance

```bash
PUT /api/meetings/64f789def123abc456/confirm
Authorization: Bearer <JWT_TOKEN>
```

## 🔒 Security Features

### Authentication & Authorization

1. **JWT Token Validation**: All protected routes validate JWT tokens through `AuthTokenFilter`
2. **Role-based Authorization**: Method-level security using `@PreAuthorize` annotations
3. **Password Security**: BCrypt encryption with salt for secure password storage
4. **CORS Configuration**: Configurable cross-origin resource sharing for web client support
5. **Authentication Middleware**: Custom JWT processing filter integrated with Spring Security

### Security Headers

The application automatically includes security headers:

- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`

### Token Management

- **Token Expiration**: 24 hours (configurable)
- **Token Format**: `Bearer <JWT_TOKEN>`
- **Token Claims**: Username, roles, issued time, expiration time
- **Secret Key**: Configurable in `application.properties`

### Access Control

#### Public Endpoints (No Authentication)

- Health checks and public information
- User registration and login

#### Protected Endpoints (Authentication Required)

- Profile management
- All meeting operations
- Role-specific dashboards

#### Role-based Restrictions

- **INVESTOR** role: Access to investor-specific endpoints
- **ENTREPRENEUR** role: Access to entrepreneur-specific endpoints
- **Meeting Organizers**: Can update/cancel their own meetings
- **Meeting Participants**: Can view and confirm meetings they're invited to

## 👥 User Roles & Permissions

### INVESTOR Role

**Dashboard Access:**

- Investment opportunities overview
- Portfolio management interface
- Investor-specific meeting views

**Capabilities:**

- View investment opportunities
- Manage investment portfolio
- Schedule meetings with entrepreneurs
- Access entrepreneur profiles and pitches
- Confirm meeting attendance

**Restrictions:**

- Cannot submit pitches
- Cannot access entrepreneur-specific funding tools

### ENTREPRENEUR Role

**Dashboard Access:**

- Funding opportunities overview
- Startup management tools
- Entrepreneur-specific meeting views

**Capabilities:**

- Submit business pitches
- Manage startup information
- Schedule meetings with investors
- Access funding opportunities
- Confirm meeting attendance

**Restrictions:**

- Cannot view investment portfolio tools
- Cannot access investor-specific opportunities

### Common Permissions (Both Roles)

- **Profile Management**: Update bio, portfolio, preferences
- **Meeting Management**: Create, view, update (if organizer), confirm attendance
- **Authentication**: Login, logout, profile access
- **Security**: Change password, update account settings

## 🚀 Getting Started

### Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **MongoDB 4.4+** running on `localhost:27017`
- **Git** for cloning the repository

### Installation & Setup

1. **Clone the repository**:

   ```bash
   git clone https://github.com/MuhammadHamza2003/Nexus-Investor-Entrepreneur-Collaboration-Platform.git
   cd Nexus-Investor-Entrepreneur-Collaboration-Platform
   ```

2. **Start MongoDB**:

   ```bash
   # On Windows
   net start MongoDB

   # On macOS/Linux
   sudo systemctl start mongod
   # or
   brew services start mongodb-community
   ```

3. **Verify MongoDB is running**:

   ```bash
   mongo --eval "db.adminCommand('ismaster')"
   ```

4. **Build the application**:

   ```bash
   ./mvnw clean compile
   ```

5. **Run the application**:

   ```bash
   ./mvnw spring-boot:run
   ```

   Or using the task from VS Code:

   ```bash
   # Use the configured VS Code task: "Run Spring Boot Application"
   ```

6. **Verify the application is running**:
   ```bash
   curl http://localhost:8080/api/public/health
   ```

### Quick Test

Test the API with a simple health check:

```bash
curl -X GET http://localhost:8080/api/public/test
```

Expected response:

```json
{
  "message": "Public API is working!",
  "description": "This endpoint is accessible without authentication"
}
```

## 📊 Database Schema

### User Collection

```javascript
{
    "_id": ObjectId,
    "username": String (unique, required),
    "email": String (unique, required),
    "password": String (BCrypt encrypted, required),
    "firstName": String (required),
    "lastName": String (required),
    "role": "INVESTOR" | "ENTREPRENEUR" (required),
    "bio": String (optional),
    "portfolio": String (optional),
    "preferences": String (optional),
    "profilePicture": String (optional),
    "phoneNumber": String (optional),
    "location": String (optional),
    "enabled": Boolean (default: true),
    "accountNonExpired": Boolean (default: true),
    "accountNonLocked": Boolean (default: true),
    "credentialsNonExpired": Boolean (default: true),
    "createdAt": DateTime (auto-generated),
    "updatedAt": DateTime (auto-updated)
}
```

### Meeting Collection

```javascript
{
    "_id": ObjectId,
    "title": String (required),
    "description": String (optional),
    "startTime": DateTime (required, must be future),
    "endTime": DateTime (required),
    "organizerId": String (required, User ID),
    "participantIds": [String] (required, Array of User IDs),
    "status": "SCHEDULED" | "CONFIRMED" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED" | "RESCHEDULED",
    "meetingLink": String (optional, for virtual meetings),
    "location": String (optional, for physical meetings),
    "agenda": String (optional),
    "notes": String (optional),
    "createdAt": DateTime (auto-generated),
    "updatedAt": DateTime (auto-updated)
}
```

### Indexes

The following indexes are recommended for optimal performance:

```javascript
// User Collection Indexes
db.users.createIndex({ username: 1 }, { unique: true });
db.users.createIndex({ email: 1 }, { unique: true });
db.users.createIndex({ role: 1 });

// Meeting Collection Indexes
db.meetings.createIndex({ organizerId: 1 });
db.meetings.createIndex({ participantIds: 1 });
db.meetings.createIndex({ startTime: 1 });
db.meetings.createIndex({ status: 1 });
db.meetings.createIndex({ organizerId: 1, startTime: 1 });
db.meetings.createIndex({ participantIds: 1, startTime: 1 });
```

## ⚠️ Conflict Detection & Validation

The meeting system implements sophisticated conflict detection to ensure scheduling integrity:

### Time Overlap Prevention

1. **Participant Conflict Check**: Prevents scheduling meetings that overlap with existing ones for any participant
2. **Organizer Inclusion**: Automatically includes the meeting organizer in participant conflict checks
3. **Multi-participant Validation**: Validates scheduling conflicts for all involved users simultaneously
4. **Status-based Filtering**: Only considers active meetings (SCHEDULED, CONFIRMED, IN_PROGRESS) for conflict detection

### Validation Rules

```java
// Meeting time validation
- startTime must be in the future
- endTime must be after startTime
- Meeting duration must be reasonable (configurable)

// Participant validation
- At least one participant required
- Organizer automatically included in participants
- All participant IDs must reference valid users

// Conflict detection algorithm
- Checks for time overlaps: (newStart < existingEnd) && (newEnd > existingStart)
- Validates against all active meetings for all participants
- Provides detailed conflict information in error responses
```

### Error Handling

When conflicts are detected, the API returns detailed error information:

```json
{
  "error": "Meeting conflict detected",
  "message": "Meeting time conflicts with existing meeting: 'Board Meeting' from 10:00 to 11:30",
  "conflictDetails": {
    "conflictingMeetingId": "64f123abc456def789",
    "conflictingMeetingTitle": "Board Meeting",
    "conflictingParticipant": "John Investor",
    "suggestedAlternatives": ["14:00-15:00", "16:00-17:00"]
  },
  "timestamp": 1692800400000
}
```

## 🎯 Project Status & Milestones

### ✅ COMPLETED FEATURES

#### Core Authentication System

- ✅ **JWT-based Authentication** - Stateless token authentication
- ✅ **User Registration** - Secure user account creation
- ✅ **User Login** - Authentication with username/email
- ✅ **Role-based Access Control** - INVESTOR vs ENTREPRENEUR roles
- ✅ **Profile Management** - Update bio, portfolio, preferences
- ✅ **Password Security** - BCrypt encryption with salt

#### Meeting Management System

- ✅ **Meeting Creation** - Schedule meetings with participants
- ✅ **Meeting Updates** - Modify meeting details (organizer only)
- ✅ **Meeting Cancellation** - Cancel meetings (organizer only)
- ✅ **Meeting Confirmation** - Participants can confirm attendance
- ✅ **Upcoming Meetings** - View future scheduled meetings
- ✅ **Conflict Detection** - Prevent overlapping meeting schedules
- ✅ **Multi-participant Support** - Include multiple attendees

#### Security & Infrastructure

- ✅ **Spring Security Integration** - Comprehensive security framework
- ✅ **MongoDB Integration** - NoSQL database persistence
- ✅ **CORS Configuration** - Cross-origin resource sharing
- ✅ **Error Handling** - Comprehensive error responses
- ✅ **Validation** - Request payload validation
- ✅ **Logging** - Debug and audit logging

### 🚧 CURRENT MILESTONE STATUS

**Milestone 3: Backend APIs** - ✅ **COMPLETED**

All core requirements have been successfully implemented:

- ✅ Secure JWT-based authentication system
- ✅ Role-based access control (Investor vs Entrepreneur)
- ✅ User registration and login APIs
- ✅ Profile management APIs (bio, portfolio, preferences)
- ✅ Meeting scheduling and management system
- ✅ Protected routes with JWT middleware
- ✅ Proper backend repository structure
- ✅ Comprehensive API documentation

### 🔮 FUTURE ENHANCEMENTS

#### Milestone 4: Advanced Features

- 🔄 **Email Verification** - Account verification via email
- 🔄 **Password Reset** - Forgot password functionality
- 🔄 **Refresh Tokens** - Token refresh mechanism
- 🔄 **Two-Factor Authentication** - Enhanced security with 2FA
- 🔄 **Rate Limiting** - API request rate limiting
- 🔄 **Audit Logging** - User activity tracking

#### Milestone 5: Integration & Deployment

- 🔄 **Frontend Integration** - React/Angular frontend
- 🔄 **Email Service** - SMTP integration for notifications
- 🔄 **File Upload** - Profile picture and document upload
- 🔄 **Social Login** - Google, LinkedIn, GitHub integration
- 🔄 **Cloud Deployment** - AWS/Azure deployment
- 🔄 **CI/CD Pipeline** - Automated testing and deployment

#### Milestone 6: Business Logic

- 🔄 **Investment Tracking** - Portfolio management features
- 🔄 **Pitch Management** - Startup pitch submission and review
- 🔄 **Notification System** - Real-time notifications
- 🔄 **Search & Filter** - Advanced search capabilities
- 🔄 **Analytics Dashboard** - Business metrics and insights
- 🔄 **Document Management** - Contract and document handling

### 📊 Technical Metrics

- **API Endpoints**: 20+ endpoints implemented
- **Security**: JWT + Role-based authorization
- **Database**: MongoDB with optimized schemas
- **Code Coverage**: Comprehensive error handling
- **Documentation**: Complete API documentation
- **Architecture**: Clean, modular Spring Boot architecture

## Meeting Management System - Issue Resolution

### Issue: Organizer Cannot See Scheduled Meetings

**Problem**: When a meeting organizer calls the `/api/meetings` endpoint, they receive an empty array `[]`, while participants can see the scheduled meetings.

**Root Cause**: The organizer was not being automatically included in the `participantIds` list when creating meetings, which caused the MongoDB query to fail when searching for meetings where the organizer should be included as a participant.

**Solution**: Modified the `MeetingService.createMeeting()` method to automatically include the organizer in the `participantIds` list if they are not already present. This ensures that:

1. **Organizers can see their own meetings** - Since they are included in participantIds
2. **Participants can see meetings they're invited to** - As before
3. **The MongoDB query works correctly** - The `findByUserInvolved` query can find meetings for both organizers and participants

### Changes Made

1. **Auto-include organizer in participants**: Modified meeting creation to automatically add the organizer to the participant list
2. **Updated conflict detection**: Ensured conflict detection handles the organizer properly
3. **Meeting updates**: Updated the meeting update logic to maintain organizer inclusion

### Fixed Code Locations

- `MeetingService.createMeeting()` - Auto-includes organizer in participantIds
- `MeetingService.updateMeeting()` - Maintains organizer inclusion during updates
- Conflict detection logic - Properly handles organizer as participant

### Testing

After this fix:

- ✅ Organizers can see meetings they organize
- ✅ Participants can see meetings they're invited to
- ✅ Both organizers and participants appear in meeting participant lists
- ✅ Conflict detection works for all involved users
