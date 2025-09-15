# Nexus Application Deployment Guide

This guide provides comprehensive instructions for deploying the Nexus Investor-Entrepreneur Collaboration Platform to various cloud platforms.

## Deployment Options

### 1. Render.com (Recommended for Free Tier)

### 2. Heroku

### 3. Railway.app

### 4. AWS (Elastic Beanstalk)

### 5. Google Cloud Platform

---

## Option 1: Deploy to Render.com

Render.com offers excellent free tier hosting for Spring Boot applications with automatic HTTPS and continuous deployment.

### Prerequisites

- GitHub account with your code pushed
- Render.com account (free tier available)
- MongoDB Atlas account (free tier available)

### Step 1: Setup MongoDB Atlas

1. **Create MongoDB Atlas Account**

   - Go to https://cloud.mongodb.com/
   - Sign up for free account
   - Create a new project: "Nexus-Production"

2. **Create Database Cluster**

   - Choose "Build a Database"
   - Select "M0 Sandbox" (Free tier)
   - Choose your preferred region
   - Cluster Name: "nexus-cluster"

3. **Configure Database Access**

   - Go to "Database Access" → "Add New Database User"
   - Username: `nexus_admin`
   - Password: Generate a secure password
   - Built-in Role: "Atlas admin" (or "Read and write to any database")

4. **Configure Network Access**

   - Go to "Network Access" → "Add IP Address"
   - Add `0.0.0.0/0` (Allow access from anywhere)
   - Or add specific IPs for better security

5. **Get Connection String**
   - Go to "Database" → "Connect" → "Connect your application"
   - Choose "Java" and version "4.3 or later"
   - Copy connection string (replace `<password>` with your password)
   - Example: `mongodb+srv://nexus_admin:<password>@nexus-cluster.xxxxx.mongodb.net/nexus_prod?retryWrites=true&w=majority`

### Step 2: Deploy to Render

1. **Connect Repository**

   - Go to https://render.com/
   - Sign up/Login with GitHub
   - Click "New" → "Web Service"
   - Connect your GitHub repository

2. **Configure Service**

   - **Name**: `nexus-api`
   - **Region**: Choose closest to your users
   - **Branch**: `main` (or your deployment branch)
   - **Runtime**: `Java`
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -jar target/Nexus-*.jar --spring.profiles.active=prod`

3. **Environment Variables**

   ```
   MONGODB_URI=mongodb+srv://nexus_admin:<password>@nexus-cluster.xxxxx.mongodb.net/nexus_prod?retryWrites=true&w=majority
   JWT_SECRET=nexusSecretKeyForAuthenticationAndAuthorization2024SuperSecureKeyWith512BitsMinimumRequiredForHS512AlgorithmJWTSecurityStandardCompliance
   JWT_EXPIRATION=86400000
   PORT=8080
   SPRING_PROFILES_ACTIVE=prod
   BASE_URL=https://nexus-api.onrender.com
   SOCKETIO_HOST=0.0.0.0
   SOCKETIO_PORT=9092
   ```

4. **Advanced Settings**

   - **Instance Type**: Free (or upgrade as needed)
   - **Auto-Deploy**: Yes (for continuous deployment)

5. **Deploy**
   - Click "Create Web Service"
   - Wait for deployment (usually 5-10 minutes)
   - Your API will be available at: `https://nexus-api.onrender.com`

### Step 3: Verify Deployment

Test your deployed API:

```bash
# Health check
curl https://nexus-api.onrender.com/api/public/health

# Swagger UI
curl https://nexus-api.onrender.com/swagger-ui/index.html

# Test registration
curl -X POST https://nexus-api.onrender.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User",
    "role": "INVESTOR"
  }'
```

---

## Option 2: Deploy to Heroku

### Prerequisites

- Heroku CLI installed
- Heroku account
- MongoDB Atlas (same setup as Render)

### Step 1: Install Heroku CLI

```bash
# Windows (using chocolatey)
choco install heroku-cli

# Mac (using homebrew)
brew tap heroku/brew && brew install heroku

# Or download from https://devcenter.heroku.com/articles/heroku-cli
```

### Step 2: Create Heroku App

```bash
# Login to Heroku
heroku login

# Create new app
heroku create nexus-api-prod

# Add Java buildpack
heroku buildpacks:set heroku/java -a nexus-api-prod
```

### Step 3: Configure Environment Variables

```bash
heroku config:set MONGODB_URI="mongodb+srv://nexus_admin:<password>@nexus-cluster.xxxxx.mongodb.net/nexus_prod?retryWrites=true&w=majority" -a nexus-api-prod

heroku config:set JWT_SECRET="nexusSecretKeyForAuthenticationAndAuthorization2024SuperSecureKeyWith512BitsMinimumRequiredForHS512AlgorithmJWTSecurityStandardCompliance" -a nexus-api-prod

heroku config:set JWT_EXPIRATION=86400000 -a nexus-api-prod

heroku config:set SPRING_PROFILES_ACTIVE=prod -a nexus-api-prod

heroku config:set BASE_URL="https://nexus-api-prod.herokuapp.com" -a nexus-api-prod
```

### Step 4: Create Procfile

```bash
# Create Procfile in project root
echo "web: java -jar target/Nexus-*.jar --server.port=\$PORT --spring.profiles.active=prod" > Procfile
```

### Step 5: Deploy

```bash
# Initialize git (if not already done)
git init
git add .
git commit -m "Initial commit for Heroku deployment"

# Add Heroku remote
heroku git:remote -a nexus-api-prod

# Deploy
git push heroku main
```

### Step 6: Scale and Monitor

```bash
# Scale web dynos
heroku ps:scale web=1 -a nexus-api-prod

# View logs
heroku logs --tail -a nexus-api-prod

# Open in browser
heroku open -a nexus-api-prod
```

---

## Option 3: Deploy to Railway.app

Railway offers simple deployment with automatic HTTPS and good free tier.

### Step 1: Setup Railway

1. Go to https://railway.app/
2. Sign up with GitHub
3. Click "Deploy from GitHub repo"
4. Select your repository

### Step 2: Configure Environment Variables

In Railway dashboard, go to Variables tab and add:

```
MONGODB_URI=mongodb+srv://nexus_admin:<password>@nexus-cluster.xxxxx.mongodb.net/nexus_prod?retryWrites=true&w=majority
JWT_SECRET=nexusSecretKeyForAuthenticationAndAuthorization2024SuperSecureKeyWith512BitsMinimumRequiredForHS512AlgorithmJWTSecurityStandardCompliance
JWT_EXPIRATION=86400000
SPRING_PROFILES_ACTIVE=prod
PORT=8080
```

### Step 3: Deploy

- Railway will automatically detect Java project
- Build and deployment happen automatically
- Your app will be available at: `https://nexus-api-production.up.railway.app`

---

## Option 4: AWS Elastic Beanstalk

### Prerequisites

- AWS CLI installed and configured
- AWS account

### Step 1: Install EB CLI

```bash
pip install awsebcli
```

### Step 2: Initialize Elastic Beanstalk

```bash
# In project root directory
eb init

# Follow prompts:
# - Select region
# - Application name: nexus-api
# - Platform: Java
# - Platform version: Java 21
# - CodeCommit: No
```

### Step 3: Create Environment

```bash
# Create production environment
eb create nexus-prod

# Set environment variables
eb setenv MONGODB_URI="mongodb+srv://nexus_admin:<password>@nexus-cluster.xxxxx.mongodb.net/nexus_prod?retryWrites=true&w=majority"
eb setenv JWT_SECRET="nexusSecretKeyForAuthenticationAndAuthorization2024SuperSecureKeyWith512BitsMinimumRequiredForHS512AlgorithmJWTSecurityStandardCompliance"
eb setenv SPRING_PROFILES_ACTIVE=prod
```

### Step 4: Deploy

```bash
# Build JAR file
./mvnw clean package -DskipTests

# Deploy
eb deploy
```

---

## Option 5: Google Cloud Platform (App Engine)

### Prerequisites

- Google Cloud SDK installed
- GCP project created

### Step 1: Create app.yaml

```yaml
runtime: java21
env: standard

instance_class: F1
automatic_scaling:
  min_instances: 0
  max_instances: 2

env_variables:
  MONGODB_URI: "mongodb+srv://nexus_admin:<password>@nexus-cluster.xxxxx.mongodb.net/nexus_prod?retryWrites=true&w=majority"
  JWT_SECRET: "nexusSecretKeyForAuthenticationAndAuthorization2024SuperSecureKeyWith512BitsMinimumRequiredForHS512AlgorithmJWTSecurityStandardCompliance"
  SPRING_PROFILES_ACTIVE: "prod"
```

### Step 2: Deploy

```bash
# Build application
./mvnw clean package -DskipTests

# Deploy to App Engine
gcloud app deploy

# View logs
gcloud app logs tail -s default
```

---

## Production Configuration

### Environment Variables Reference

| Variable                 | Description                       | Example                                          |
| ------------------------ | --------------------------------- | ------------------------------------------------ |
| `MONGODB_URI`            | MongoDB connection string         | `mongodb+srv://user:pass@cluster.mongodb.net/db` |
| `JWT_SECRET`             | JWT signing secret (min 512 bits) | `your-super-secure-secret-key`                   |
| `JWT_EXPIRATION`         | Token expiration in milliseconds  | `86400000` (24 hours)                            |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile             | `prod`                                           |
| `PORT`                   | Server port (set by platform)     | `8080`                                           |
| `BASE_URL`               | Base URL of your application      | `https://your-app.com`                           |
| `SOCKETIO_HOST`          | Socket.IO bind address            | `0.0.0.0`                                        |
| `SOCKETIO_PORT`          | Socket.IO port                    | `9092`                                           |

### Security Considerations

1. **JWT Secret**: Use a strong, unique secret (minimum 512 bits for HS512)
2. **Database Access**: Restrict MongoDB network access to your application IPs
3. **HTTPS**: Always use HTTPS in production (most platforms provide this automatically)
4. **Environment Variables**: Never commit secrets to version control
5. **Error Messages**: Production mode hides detailed error messages

### Monitoring and Logging

1. **Application Logs**: Monitor application startup and runtime logs
2. **Health Checks**: Use `/actuator/health` for monitoring
3. **Database Monitoring**: Monitor MongoDB Atlas metrics
4. **Performance**: Monitor response times and throughput

### Custom Domain Setup (Optional)

For production applications, you may want to use a custom domain:

#### Render.com

1. Go to Settings → Custom Domains
2. Add your domain (e.g., `api.nexusplatform.com`)
3. Configure DNS CNAME to point to Render

#### Heroku

```bash
# Add custom domain
heroku domains:add api.nexusplatform.com -a nexus-api-prod

# Configure DNS
# CNAME: api.nexusplatform.com → nexus-api-prod.herokuapp.com
```

---

## Testing Production Deployment

### Update Test Script for Production

```powershell
# Update base URL in test script
$baseUrl = "https://nexus-api.onrender.com"  # Replace with your deployment URL

# Run tests against production
.\API Testing\milestone-8-comprehensive-test.ps1
```

### Postman Collection for Production

1. Import the Postman collection
2. Update environment variables:
   - `base_url`: `https://your-deployed-app.com`
3. Run the collection to validate all endpoints

---

## Troubleshooting

### Common Deployment Issues

1. **Build Failures**

   - Ensure Java 21 is specified
   - Check Maven wrapper permissions
   - Verify all dependencies are available

2. **Database Connection**

   - Verify MongoDB URI format
   - Check network access settings in MongoDB Atlas
   - Ensure credentials are correct

3. **Environment Variables**

   - Verify all required variables are set
   - Check for typos in variable names
   - Ensure JWT secret is long enough

4. **Port Configuration**
   - Use `${PORT:8080}` in application.properties
   - Don't hardcode ports in production

### Health Check Endpoint

Monitor your deployment using:

```bash
curl https://your-app.com/actuator/health
```

Expected response:

```json
{
  "status": "UP",
  "components": {
    "mongo": {
      "status": "UP"
    }
  }
}
```

---

## Cost Optimization

### Free Tier Limits

- **Render.com**: 750 hours/month (free tier)
- **Heroku**: 1000 dyno hours/month (verified account)
- **Railway**: $5 credit/month
- **MongoDB Atlas**: 512MB storage (M0 tier)

### Scaling Considerations

- Start with free tiers for development/testing
- Monitor usage and upgrade as needed
- Use auto-scaling features for production workloads

---

## Next Steps

After successful deployment:

1. **Configure CI/CD**: Set up automatic deployments on code changes
2. **Add Monitoring**: Implement application performance monitoring
3. **Setup Backups**: Configure MongoDB backups
4. **Custom Domain**: Configure custom domain if needed
5. **SSL Certificate**: Ensure HTTPS is properly configured
6. **Load Testing**: Test application under load
7. **Documentation**: Update API documentation with production URLs
