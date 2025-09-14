# Nexus Deployment Guide

This document provides comprehensive instructions for deploying the Nexus application in production.

## Prerequisites

- Docker and Docker Compose installed
- Java 21 or higher (for local development)
- MongoDB 7.0+ (if running without Docker)
- Valid Stripe account with API keys

## Quick Start with Docker

1. **Clone and Navigate**

   ```bash
   git clone <repository-url>
   cd nexus
   ```

2. **Environment Setup**

   ```bash
   # Copy the environment template
   cp .env.template .env

   # Edit .env with your actual values
   nano .env
   ```

3. **Deploy with Docker Compose**

   ```bash
   # Build and start all services
   docker-compose up -d

   # Check service status
   docker-compose ps

   # View logs
   docker-compose logs -f nexus-app
   ```

4. **Access the Application**
   - Main Application: http://localhost:8080
   - Socket.IO: ws://localhost:9092
   - MongoDB: localhost:27017

## Manual Deployment

### 1. Database Setup

**MongoDB Installation:**

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install mongodb-org

# Start MongoDB service
sudo systemctl start mongod
sudo systemctl enable mongod
```

**Database Initialization:**

```bash
# Connect to MongoDB
mongosh

# Run initialization script
load('init-mongo.js')
```

### 2. Application Setup

**Build the Application:**

```bash
# Using Maven wrapper
./mvnw clean package -DskipTests

# Or with system Maven
mvn clean package -DskipTests
```

**Configure Application:**

```bash
# Copy production properties
cp src/main/resources/application-prod.properties application.properties

# Set environment variables
export MONGODB_URI="mongodb://localhost:27017/nexus_prod"
export JWT_SECRET="your-secret-key"
export STRIPE_API_KEY="sk_live_your_key"
```

**Run the Application:**

```bash
java -jar target/nexus-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## Environment Variables

### Required Variables

| Variable         | Description                       | Example                                |
| ---------------- | --------------------------------- | -------------------------------------- |
| `MONGODB_URI`    | MongoDB connection string         | `mongodb://localhost:27017/nexus_prod` |
| `JWT_SECRET`     | JWT signing secret (min 512 bits) | `your-super-secure-secret-key`         |
| `STRIPE_API_KEY` | Stripe secret key                 | `sk_live_...` or `sk_test_...`         |

### Optional Variables

| Variable                | Default                 | Description                    |
| ----------------------- | ----------------------- | ------------------------------ |
| `JWT_EXPIRATION`        | `86400000`              | JWT token expiration (ms)      |
| `BASE_URL`              | `http://localhost:8080` | Application base URL           |
| `PORT`                  | `8080`                  | Server port                    |
| `STRIPE_WEBHOOK_SECRET` | -                       | Stripe webhook endpoint secret |
| `DEFAULT_CURRENCY`      | `USD`                   | Default transaction currency   |
| `TRANSACTION_FEE`       | `0.029`                 | Transaction fee percentage     |
| `FIXED_FEE`             | `0.30`                  | Fixed fee amount               |

## Production Checklist

### Security

- [ ] Update JWT secret to a strong, random value
- [ ] Use production Stripe API keys
- [ ] Enable HTTPS/TLS certificates
- [ ] Configure firewall rules
- [ ] Set up MongoDB authentication
- [ ] Update default passwords

### Performance

- [ ] Configure MongoDB indexes (handled by init script)
- [ ] Set up connection pooling
- [ ] Configure caching (Redis recommended)
- [ ] Optimize JVM heap size
- [ ] Set up monitoring and logging

### Monitoring

- [ ] Set up health checks
- [ ] Configure log aggregation
- [ ] Set up application metrics
- [ ] Configure alerts and notifications
- [ ] Database backup strategy

## Health Checks

The application provides several health check endpoints:

- **Main Health Check:** `GET /actuator/health`
- **Database Check:** Included in main health check
- **Payment System:** `GET /api/payments/health`

## Backup Strategy

### Database Backup

```bash
# Create backup
mongodump --host localhost:27017 --db nexus_prod --out /backup/$(date +%Y%m%d)

# Restore backup
mongorestore --host localhost:27017 --db nexus_prod /backup/20240101/nexus_prod/
```

### File Backup

```bash
# Backup uploaded files
tar -czf /backup/uploads-$(date +%Y%m%d).tar.gz ./uploads/
```

## Troubleshooting

### Common Issues

1. **Application Won't Start**

   ```bash
   # Check Java version
   java -version

   # Check MongoDB connection
   mongosh $MONGODB_URI

   # Check logs
   docker-compose logs nexus-app
   ```

2. **Payment Processing Issues**

   ```bash
   # Verify Stripe configuration
   curl -u $STRIPE_API_KEY: https://api.stripe.com/v1/account

   # Check webhook configuration
   curl -H "Authorization: Bearer $STRIPE_API_KEY" \
        https://api.stripe.com/v1/webhook_endpoints
   ```

3. **Database Connection Issues**

   ```bash
   # Test MongoDB connection
   mongosh $MONGODB_URI --eval "db.adminCommand('ping')"

   # Check MongoDB logs
   sudo journalctl -u mongod
   ```

### Log Locations

- **Application Logs:** `docker-compose logs nexus-app`
- **MongoDB Logs:** `/var/log/mongodb/mongod.log`
- **Nginx Logs:** `/var/log/nginx/access.log`, `/var/log/nginx/error.log`

## Performance Tuning

### JVM Options

```bash
export JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### MongoDB Optimization

```javascript
// Enable profiling for slow queries
db.setProfilingLevel(2, { slowms: 100 });

// Monitor index usage
db.transactions.aggregate([{ $indexStats: {} }]);
```

## Scaling Considerations

### Horizontal Scaling

- Use MongoDB replica sets for high availability
- Implement application clustering with load balancer
- Consider Redis for session management
- Use CDN for file uploads

### Vertical Scaling

- Increase JVM heap size based on usage
- Optimize MongoDB memory usage
- Use SSD storage for better I/O performance

## Support

For deployment issues or questions:

1. Check the troubleshooting section
2. Review application logs
3. Verify environment configuration
4. Contact the development team

---

**Last Updated:** $(date)
**Version:** 1.0.0
