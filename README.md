# Nexus - Investor-Entrepreneur Collaboration Platform

## 🚀 Overview

Nexus is a comprehensive Spring Boot application designed to facilitate seamless collaboration between investors and entrepreneurs. The platform provides secure authentication, meeting management, and real-time video calling capabilities through WebRTC and Socket.IO integration.

## 📋 Table of Contents

- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Real-time Communication](#-real-time-communication)
- [Testing](#-testing)
- [Configuration](#-configuration)
- [Contributing](#-contributing)
- [License](#-license)

## ✨ Features

### 🔐 Authentication & Authorization

- JWT-based secure authentication
- Role-based access control (INVESTOR/ENTREPRENEUR)
- User registration and profile management
- Password encryption with Spring Security

### 👥 User Management

- User registration with role selection
- Profile management and updates
- Role-specific dashboard access
- Secure user data handling

### 📅 Meeting Management

- Create, update, and delete meetings with comprehensive validation
- Schedule meetings between investors and entrepreneurs
- Advanced meeting status tracking (SCHEDULED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, RESCHEDULED, ENDED)
- Intelligent conflict detection across all participants
- Participant management with automatic organizer inclusion
- Meeting search functionality with title-based filtering
- Upcoming meetings with real-time status updates
- Meeting confirmation workflow for participants
- Flexible rescheduling with conflict validation

### 🎥 Video Calling System

- Real-time video room creation and management
- WebRTC-based peer-to-peer communication
- Socket.IO for signaling and real-time events
- Join/leave room functionality
- ICE candidate exchange for optimal connectivity

### 🌐 Real-time Communication

- Socket.IO server integration
- WebRTC signaling for video calls
- Real-time user presence tracking
- Event-driven communication architecture

## 🛠 Technology Stack

### Backend

- **Java 21** - Latest LTS version
- **Spring Boot 3.5.4** - Main framework
- **Spring Security** - Authentication and authorization
- **Spring Data MongoDB** - Database operations
- **MongoDB** - NoSQL database
- **Socket.IO (Netty)** - Real-time communication
- **WebSocket** - Additional real-time support
- **JWT (JJWT)** - Token-based authentication
- **Lombok** - Code generation
- **Maven** - Dependency management

### Testing & Quality

- **JUnit 5** - Unit testing framework
- **Spring Boot Test** - Integration testing
- **PowerShell Scripts** - API testing automation
- **Comprehensive test suites** - Load, security, and functional tests

## 🏗 Architecture

### Package Structure

```
src/main/java/com/Nexus/Nexus/
├── config/           # Configuration classes
│   ├── SocketIOConfig.java
│   └── WebSecurityConfig.java
├── controller/       # REST controllers
│   ├── AuthController.java
│   ├── MeetingController.java
│   ├── VideoRoomController.java
│   ├── InvestorController.java
│   ├── EntrepreneurController.java
│   ├── PublicController.java
│   └── TestController.java
├── dto/             # Data Transfer Objects
│   ├── AuthResponse.java
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── MeetingCreateRequest.java
│   ├── MeetingResponse.java
│   ├── VideoRoomCreateRequest.java
│   └── VideoRoomResponse.java
├── model/           # Entity models
│   ├── User.java
│   ├── Meeting.java
│   ├── VideoRoom.java
│   ├── Role.java
│   └── MeetingStatus.java
├── repository/      # Data access layer
│   ├── UserRepository.java
│   ├── MeetingRepository.java
│   └── VideoRoomRepository.java
├── security/        # Security configurations
├── service/         # Business logic
│   ├── AuthService.java
│   ├── MeetingService.java
│   ├── VideoRoomService.java
│   ├── SignalingService.java
│   └── UserDetailsServiceImpl.java
└── NexusApplication.java
```

### Database Schema

#### MongoDB Collections

**Users Collection** (`users`)

- User profiles with role-based access (INVESTOR/ENTREPRENEUR)
- Fields: id, username, email, password, firstName, lastName, role, bio, portfolio, preferences, etc.
- Indexed on username and email for unique constraints

**Meetings Collection** (`meetings`)

- Meeting scheduling and management with conflict detection
- Fields: id, title, description, startTime, endTime, organizerId, participantIds, status, meetingLink, location, agenda, notes
- Status tracking: SCHEDULED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, RESCHEDULED, ENDED
- Advanced querying for conflicts, upcoming meetings, and user-specific meetings

**Video Rooms Collection** (`video_rooms`)

- Video call session management with WebRTC support
- Fields: id, roomId, meetingId, hostId, participantIds, activeParticipants, signalingHistory
- Real-time participant tracking and signaling message storage

### Repository Layer

#### UserRepository

- User management with unique constraints on username and email
- Role-based user queries for investors and entrepreneurs
- Support for authentication via username or email

#### MeetingRepository

- Advanced MongoDB queries for meeting management
- Conflict detection queries for scheduling validation
- User-specific meeting retrieval (organizer or participant)
- Search functionality with case-insensitive title matching
- Time-based queries for upcoming meetings and date ranges

#### VideoRoomRepository

- Video room lifecycle management
- Participant tracking and room state management
- Integration with meeting data for access control

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6+
- MongoDB 4.4+
- Node.js (for frontend integration)

### Installation

1. **Clone the repository**

   ```bash
   git clone https://github.com/MuhammadHamza2003/Nexus-Investor-Entrepreneur-Collaboration-Platform.git
   cd Video\ call
   ```

2. **Configure MongoDB**

   - Start MongoDB service
   - Default connection: `localhost:27017`
   - Database name: `nexus`

3. **Update Configuration**

   ```properties
   # src/main/resources/application.properties
   spring.data.mongodb.host=localhost
   spring.data.mongodb.port=27017
   spring.data.mongodb.database=nexus
   ```

4. **Build the application**

   ```bash
   ./mvnw clean install
   ```

5. **Run the application**

   ```bash
   ./mvnw spring-boot:run
   ```

6. **Access the application**
   - REST API: `http://localhost:8080`
   - Socket.IO Server: `http://localhost:9092`

## 📚 API Documentation

### Public Endpoints

- `GET /api/public/test` - Test endpoint
- `GET /api/public/health` - Health check

### Test Endpoints

- `POST /api/test/echo` - Echo test with payload
- `GET /api/test/status` - Service status and available endpoints

### Authentication Endpoints

- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /api/auth/profile` - Get current user profile
- `PUT /api/auth/profile` - Update user profile

### Meeting Management

- `POST /api/meetings` - Create meeting
- `GET /api/meetings` - Get user meetings
- `GET /api/meetings/{id}` - Get specific meeting
- `PUT /api/meetings/{id}` - Update meeting
- `DELETE /api/meetings/{id}` - Cancel meeting
- `GET /api/meetings/upcoming` - Get upcoming meetings
- `GET /api/meetings/search?title={query}` - Search meetings by title
- `PUT /api/meetings/{id}/confirm` - Confirm meeting attendance

### Video Room Management

- `POST /api/video/rooms` - Create video room
- `POST /api/video/rooms/{roomId}/join` - Join video room
- `POST /api/video/rooms/{roomId}/leave` - Leave video room
- `GET /api/video/rooms/{roomId}` - Get room details
- `GET /api/video/rooms/user/{userId}` - Get user's rooms

### Role-Specific Endpoints

#### Investor Endpoints (INVESTOR role required)

- `GET /api/investor/dashboard` - Investor dashboard
- `GET /api/investor/opportunities` - Investment opportunities
- `GET /api/investor/portfolio` - Portfolio management

#### Entrepreneur Endpoints (ENTREPRENEUR role required)

- `GET /api/entrepreneur/dashboard` - Entrepreneur dashboard
- `GET /api/entrepreneur/projects` - Project management
- `GET /api/entrepreneur/pitches` - Pitch presentations

## � Data Validation & DTOs

### Request/Response Models

The application uses comprehensive DTOs (Data Transfer Objects) with validation:

#### Authentication DTOs

- **RegisterRequest**: User registration with validation for username (3-20 chars), email format, password (min 6 chars)
- **LoginRequest**: Login credentials with required username/email and password
- **AuthResponse**: JWT token response with user details
- **ProfileUpdateRequest**: Profile updates with optional password change

#### Meeting DTOs

- **MeetingCreateRequest**: Meeting creation with future date validation, participant requirements
  - Title: Required, max 100 characters
  - Description: Optional, max 500 characters
  - Start/End time: Future date validation
  - Participants: At least one required
  - Agenda: Optional, max 1000 characters
- **MeetingUpdateRequest**: Flexible meeting updates with partial validation
- **MeetingResponse**: Complete meeting information with participant names and status

#### Video Room DTOs

- **VideoRoomCreateRequest**: Room creation with meeting ID requirement
- **VideoRoomResponse**: Room details with participant tracking
- **SignalingMessageRequest**: WebRTC signaling data structure

### Validation Features

- **Jakarta Validation**: Comprehensive field validation using annotations
- **Custom Business Logic**: Meeting conflict detection, time validation
- **Error Handling**: Structured error responses with timestamps
- **Participant Validation**: Ensures all participants exist in the system

## �🔄 Real-time Communication

### Socket.IO Events

#### Connection Events

- `connect` - Client connection established
- `disconnect` - Client disconnection

#### Room Management

- `join-room` - Join a video room
- `leave-room` - Leave a video room

#### WebRTC Signaling

- `webrtc-offer` - Send WebRTC offer
- `webrtc-answer` - Send WebRTC answer
- `ice-candidate` - Exchange ICE candidates

### WebRTC Integration

The application implements WebRTC for peer-to-peer video communication:

- **Signaling Server**: Socket.IO handles signaling between peers
- **STUN/TURN Support**: Configurable for NAT traversal
- **Media Handling**: Audio/video stream management
- **Connection Management**: Automatic reconnection and error handling

## 🧪 Testing

### Test Suites Available

#### PowerShell Test Scripts

Located in `API Testing/` directory:

1. **video-room-simple-test.ps1** - Basic functionality validation
2. **video-room-load-test.ps1** - Performance and load testing
3. **video-room-security-test.ps1** - Security validation
4. **video-room-master-test.ps1** - Comprehensive test suite
5. **video-room-clean-test.ps1** - Database cleanup utilities

#### Running Tests

```bash
# Unit Tests
./mvnw test

# Integration Tests
./mvnw integration-test

# API Tests (PowerShell)
cd "API Testing"
.\video-room-simple-test.ps1
```

### Test Coverage

- Unit tests for service layer
- Integration tests for controllers
- API endpoint validation
- Security testing
- Load testing scenarios

## ⚙️ Configuration

### Environment Variables

```properties
# Database Configuration
MONGODB_HOST=localhost
MONGODB_PORT=27017
MONGODB_DATABASE=nexus

# JWT Configuration
JWT_SECRET=your-super-secure-secret-key
JWT_EXPIRATION=86400000

# Socket.IO Configuration
SOCKETIO_HOST=localhost
SOCKETIO_PORT=9092
SOCKETIO_ENABLED=true

# Server Configuration
SERVER_PORT=8080
```

### Application Properties

Key configuration files:

- `application.properties` - Main configuration
- `application-nosocket.properties` - Configuration without Socket.IO

### Security Configuration

- JWT token expiration: 24 hours (configurable)
- Password encryption: BCrypt
- CORS enabled for cross-origin requests (`origins = "*"`)
- Role-based endpoint protection with `@PreAuthorize` annotations
- Method-level security for investor and entrepreneur specific endpoints
- Secure WebSocket connections with CORS support

### Error Handling & Logging

- Comprehensive exception handling with structured error responses
- Timestamped error messages for debugging
- Debug logging for authentication and application flow
- Graceful handling of validation errors, conflicts, and access denied scenarios
- Custom error responses for different HTTP status codes (400, 403, 500)

## 🧠 Business Logic & Features

### Meeting Conflict Detection

- **Smart Scheduling**: Automatic detection of time conflicts across all participants
- **Validation Rules**: End time must be after start time, meetings must be scheduled in the future
- **Participant Verification**: Ensures all participants exist in the system before meeting creation
- **Organizer Auto-inclusion**: Automatically adds meeting organizer to participant list
- **Update Validation**: Conflict checking when rescheduling existing meetings

### Meeting Status Workflow

- **SCHEDULED** → Initial state when meeting is created
- **CONFIRMED** → Participant confirmation received
- **RESCHEDULED** → Meeting time/details updated
- **IN_PROGRESS** → Meeting is currently active
- **COMPLETED** → Meeting finished successfully
- **CANCELLED** → Meeting was cancelled
- **ENDED** → Meeting terminated (different from completed)

### User Access Control

- **Role-based Authorization**: Different access levels for INVESTOR vs ENTREPRENEUR
- **Meeting Participation**: Users can only view/modify meetings they're involved in
- **Organizer Privileges**: Only meeting organizers can update meeting details
- **Participant Rights**: Participants can confirm attendance and view meeting details

### Video Room Management

- **Dynamic Room Creation**: Rooms created on-demand linked to meetings
- **Participant Tracking**: Real-time tracking of active participants
- **Signaling History**: Storage of WebRTC signaling messages for debugging
- **Room State Management**: Active/inactive room status with automatic cleanup

## 🔧 Development

### Adding New Features

1. **Create Model**: Define entity in `model/` package
2. **Create Repository**: Add data access layer
3. **Create Service**: Implement business logic
4. **Create Controller**: Add REST endpoints
5. **Add Tests**: Create comprehensive test coverage

### Code Standards

- Use Lombok annotations for boilerplate code
- Follow Spring Boot best practices
- Implement proper error handling
- Add comprehensive logging
- Write unit and integration tests

## 🐛 Troubleshooting

### Common Issues

1. **MongoDB Connection Issues**

   - Verify MongoDB is running
   - Check connection string in properties
   - Ensure database permissions

2. **Socket.IO Connection Problems**

   - Verify port 9092 is available
   - Check firewall settings
   - Validate CORS configuration

3. **JWT Token Issues**
   - Check token expiration
   - Verify secret key configuration
   - Validate token format

## 📝 API Request Examples

### User Registration

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "investor1",
    "email": "investor@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "role": "INVESTOR"
  }'
```

### Create Meeting

```bash
curl -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "title": "Investment Discussion",
    "description": "Discussing potential investment opportunities",
    "startTime": "2024-12-01T10:00:00",
    "endTime": "2024-12-01T11:00:00",
    "participantIds": ["participant_id_1", "participant_id_2"]
  }'
```

### Create Video Room

```bash
curl -X POST http://localhost:8080/api/video/rooms \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "meetingId": "meeting_id_here",
    "maxParticipants": 10
  }'
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:

- Create an issue in the GitHub repository
- Contact the development team
- Check the documentation and test examples

## 🚦 Project Status

- ✅ Authentication & Authorization
- ✅ User Management
- ✅ Meeting Management
- ✅ Video Room Creation
- ✅ Socket.IO Integration
- ✅ WebRTC Signaling
- ✅ API Testing Suite
- 🔄 Frontend Integration (In Progress)
- 🔄 Mobile App Support (Planned)

---

**Built with ❤️ by the Nexus Development Team**
