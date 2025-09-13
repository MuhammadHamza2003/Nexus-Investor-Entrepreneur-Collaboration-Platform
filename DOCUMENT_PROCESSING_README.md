# Document Processing Chamber (APIs) - Milestone 5

## Overview

This milestone implements comprehensive document processing functionality for the Nexus platform, enabling users to upload, manage, preview, and digitally sign documents within the context of meetings and collaborations.

## Features Implemented

### 1. Document Upload API

- **Endpoint**: `POST /api/documents/upload`
- **Description**: Upload documents with support for multiple file types
- **Features**:
  - Multi-part file upload support
  - File type validation
  - Size restrictions (configurable, default 10MB)
  - Automatic file type detection
  - Metadata storage
  - Access control (viewers, editors)
  - Meeting association

**Supported File Types**:

- PDF documents (`application/pdf`)
- Microsoft Word documents (`.doc`, `.docx`)
- Text files (`.txt`)
- Images (`.jpg`, `.jpeg`, `.png`, `.gif`)

### 2. Document Retrieval APIs

#### Get Document by ID

- **Endpoint**: `GET /api/documents/{documentId}`
- **Description**: Retrieve document details and metadata
- **Access Control**: Only users with view permissions can access

#### Get User Documents

- **Endpoint**: `GET /api/documents/my-documents`
- **Description**: Get all documents accessible to the current user

#### Get Meeting Documents

- **Endpoint**: `GET /api/documents/meeting/{meetingId}`
- **Description**: Get all documents associated with a specific meeting

### 3. Document Preview API

- **Endpoint**: `GET /api/documents/{documentId}/preview`
- **Description**: Get document preview/thumbnail if available
- **Features**:
  - Thumbnail generation for images and PDFs
  - Preview availability status

### 4. Document Download API

- **Endpoint**: `GET /api/documents/download/{encodedPath}`
- **Description**: Download document files
- **Features**:
  - Secure file serving
  - Access control validation
  - Proper content headers

### 5. E-Signature APIs

#### Sign Document

- **Endpoint**: `POST /api/documents/{documentId}/sign`
- **Description**: Digitally sign a document
- **Features**:
  - Multiple signature types (Electronic, Digital, Handwritten)
  - Audit trail (IP address, timestamp, user agent)
  - Signature validation
  - Automatic status updates

#### Get Documents Requiring Signature

- **Endpoint**: `GET /api/documents/requiring-signature`
- **Description**: Get all documents that require the current user's signature

### 6. Document Management APIs

#### Delete Document

- **Endpoint**: `DELETE /api/documents/{documentId}`
- **Description**: Delete a document (owner only)
- **Features**:
  - File cleanup from storage
  - Database record removal
  - Permission validation

#### Get Document Metadata

- **Endpoint**: `GET /api/documents/{documentId}/metadata`
- **Description**: Get comprehensive document metadata

## Data Models

### DocumentEntity

Core document model with the following key fields:

- Basic info (name, description, file details)
- Access control (owner, viewers, editors)
- E-signature fields (signatures, signatories)
- Metadata and versioning
- Audit trail

### DocumentSignature

E-signature model supporting:

- Multiple signature types
- Signer information
- Timestamp and location data
- Certificate information for digital signatures

### DocumentStatus Enum

- `DRAFT`: Initial status for uploaded documents
- `REVIEWED`: Document has been reviewed
- `SIGNED`: All required signatures collected
- `ARCHIVED`: Document archived
- `DELETED`: Soft delete status

## Security Features

### Access Control

- **Owner**: Full access (read, write, delete)
- **Editors**: Can modify document content
- **Viewers**: Read-only access
- **Signatories**: Can sign the document

### Authentication

- All endpoints require JWT authentication
- User context extracted from security principal
- Role-based access where applicable

### File Security

- Secure file upload with validation
- Access control on download
- File type restrictions
- Size limitations

## Configuration

### Application Properties

```properties
# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
app.upload.dir=uploads
app.document.max-file-size=10485760
app.document.allowed-types=application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,text/plain,image/jpeg,image/png,image/gif
```

## Integration with Meeting System

### Meeting-Document Association

- Documents can be associated with specific meetings
- Meeting participants automatically get access to meeting documents
- Document requirements can be set per meeting

### Enhanced Meeting Model

Extended the Meeting model to include:

- `documentIds`: List of associated document IDs
- `documentsRequired`: Flag indicating if documents are required

## Error Handling

### Comprehensive Error Responses

- File size exceeded
- Invalid file type
- Access denied
- Document not found
- Signature validation errors

### Logging

- Detailed logging for all operations
- Error tracking and debugging
- Security event logging

## Testing

### Unit Tests

- Document controller tests
- Service layer tests
- Repository tests
- Security tests

### Integration Testing

- End-to-end API testing
- File upload/download testing
- Access control validation

## Future Enhancements

### Cloud Storage Integration

- AWS S3 integration for scalability
- Cloudinary for image processing
- CDN integration for faster access

### Advanced Document Processing

- OCR for scanned documents
- Document conversion (PDF generation)
- Version control and diff tracking
- Collaborative editing

### Enhanced E-Signatures

- Digital certificate integration
- Biometric signatures
- Multi-factor authentication for signing
- Legal compliance features

### Advanced Features

- Document templates
- Workflow automation
- Approval processes
- Document expiration
- Advanced search and indexing

## API Usage Examples

### Upload Document

```bash
curl -X POST "http://localhost:8080/api/documents/upload" \
  -H "Authorization: Bearer {jwt_token}" \
  -F "file=@document.pdf" \
  -F "name=Important Document" \
  -F "description=Document for meeting discussion" \
  -F "meetingId=meeting123" \
  -F "requiresSignature=true" \
  -F "signatoryIds=user1,user2"
```

### Sign Document

```bash
curl -X POST "http://localhost:8080/api/documents/{documentId}/sign" \
  -H "Authorization: Bearer {jwt_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "signatureData": "base64_encoded_signature",
    "signatureType": "ELECTRONIC",
    "ipAddress": "192.168.1.1",
    "userAgent": "Mozilla/5.0..."
  }'
```

### Get Documents Requiring Signature

```bash
curl -X GET "http://localhost:8080/api/documents/requiring-signature" \
  -H "Authorization: Bearer {jwt_token}"
```

This implementation provides a solid foundation for document processing in the Nexus platform, with room for future enhancements and cloud-based scaling.
