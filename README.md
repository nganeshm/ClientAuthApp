# ClientAuthApp - Spring Boot Client Authentication API


## Overview

**ClientAuthApp** is a Spring Boot REST API service that manages client registrations with automatic email notifications. The application provides CORS-enabled endpoints for client authentication and contact management, integrating seamlessly with AWS infrastructure for database and email services.

The service accepts client details through a REST API, persists them to AWS RDS MySQL database, and automatically sends personalized welcome emails to registered users.

---

## 🏗️ Architecture & Stack

### Technology Stack
- **Language:** Java 17
- **Framework:** Spring Boot 3.5.8
- **Build Tool:** Maven 3.9
- **Database:** MySQL 8.0 (via AWS RDS)
- **Containerization:** Docker (Alpine Linux)
- **ORM:** JPA/Hibernate
- **Email Provider:** SMTP (Gmail/AWS SES)

### Key Dependencies
- **spring-boot-starter-web** – RESTful API support
- **spring-boot-starter-data-jpa** – Database persistence & ORM
- **spring-boot-starter-mail** – Email sending capability
- **mysql-connector-j (8.0.33)** – MySQL database driver
- **lombok** – Reduce boilerplate code
- **eclipse-temurin:17** – Lightweight JDK for Docker

---

## 📂 Project Structure

```
src/main/java/
  ├── ClientApplication.java          Entry point with component scanning
  ├── config/
  │   └── CorsConfig.java             CORS configuration for React frontend
  ├── controller/
  │   └── ClientController.java       REST endpoints (@PostMapping /api/client/saveClient)
  ├── service/
  │   ├── ClientService.java          Business logic interface
  │   ├── ClientServiceImpl.java       Client registration & persistence
  │   ├── EmailService.java           Email interface
  │   └── EmailServiceImpl.java        Welcome email implementation
  ├── entity/
  │   └── ClientEntity.java           JPA entity mapping to 'client' table
  ├── dto/
  │   └── ClientDTO.java              Request/response data transfer object
  └── repo/
      └── ClientRepository.java       Spring Data JPA repository

src/main/resources/
  └── application.yaml                Configuration for DB, mail, logging
```

### How It Fits Together

1. **Request Flow:** React frontend sends AJAX POST request → CORS validates origin → `ClientController` receives request
2. **Processing:** `ClientController` passes DTO to `ClientServiceImpl` → validates data → converts to `ClientEntity`
3. **Persistence:** `ClientRepository` saves entity to AWS RDS MySQL database
4. **Email Trigger:** `EmailServiceImpl` sends welcome email via SMTP (asynchronous, non-blocking)
5. **Response:** Success/failure boolean returned to frontend

---

## 🔌 AWS Service Integrations

### 1. **AWS RDS (Relational Database Service) - MySQL**
- **Endpoint:** `clientdb.c7gccsuya4ux.ap-south-1.rds.amazonaws.com:3306`
- **Database:** `clientdb`
- **Configuration (application.yaml):**
  ```yaml
  spring:
    datasource:
      url: jdbc:mysql://clientdb.c7gccsuya4ux.ap-south-1.rds.amazonaws.com:3306/clientdb
      driver-class-name: com.mysql.cj.jdbc.Driver
      username: admin
      password: ${DB_PASSWORD}  # Use environment variables
  ```
- **Credentials:** Managed via environment variables (never hardcoded in production)

### 2. **AWS S3 - Static Website Hosting**
- **Origin:** `http://mywebstack2025.s3-website.ap-south-1.amazonaws.com`
- **Purpose:** Hosts React frontend that sends AJAX requests to this backend
- **Integrated via CORS** configuration (see below)

### 3. **AWS SES (Simple Email Service) - Optional Production Setup**
- **Region:** `ap-south-1` (Asia Pacific - Mumbai)
- **Advantage:** 62,000 free emails/month from EC2 instances, highly scalable
- **Configuration example:**
  ```yaml
  spring:
    mail:
      host: email-smtp.ap-south-1.amazonaws.com
      port: 587
      username: ${MAIL_USERNAME}
      password: ${MAIL_PASSWORD}
  ```

### 4. **AWS EC2 - Application Deployment**
- Dockerized application runs on EC2 instance
- Port `8081` exposed for backend API
- Security group allows inbound traffic on port 8081
- Outbound traffic to RDS (port 3306) and SMTP (port 587)

### 5. **AWS ECR (Elastic Container Registry) - Optional**
- Docker images can be pushed to ECR instead of Docker Hub
- Integrated deployment via Docker Compose or ECS

---

## 🌐 CORS Configuration

### Cross-Origin Resource Sharing (CORS)

The application implements CORS to allow the React frontend (S3 hosted) to communicate with this backend API.

**Configuration File:** `src/main/java/config/CorsConfig.java`

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("http://mywebstack2025.s3-website.ap-south-1.amazonaws.com")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```

**Key Features:**
- ✅ Allows requests from S3 origin only (whitelist specific origin)
- ✅ Supports all HTTP methods (GET, POST, PUT, DELETE, OPTIONS)
- ✅ Allows any headers (`allowedHeaders("*")`)
- ✅ Credentials (cookies, auth tokens) included in requests
- ✅ Applied globally to all endpoints (`"/**"`)

**Controller-level CORS:** Also configured at endpoint level
```java
@CrossOrigin(origins = "http://mywebstack2025.s3-website.ap-south-1.amazonaws.com")
@RestController
@RequestMapping("/api/client")
public class ClientController { ... }
```

---

## 🔄 React AJAX Integration

### Frontend-to-Backend Communication

The React frontend sends AJAX requests to the backend API endpoints.

**Endpoint:** `POST /api/client/saveClient`

**Frontend Example (React):**
```javascript
const saveClient = async (clientData) => {
  try {
    const response = await fetch(
      'http://<EC2-PUBLIC-IP>:8081/api/client/saveClient',
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include', // Include cookies/credentials
        body: JSON.stringify(clientData),
      }
    );
    
    const result = await response.json();
    console.log('Registration successful:', result);
  } catch (error) {
    console.error('Registration failed:', error);
  }
};

// Usage
saveClient({
  clientName: 'John Doe',
  clientEmail: 'john@example.com',
  clientSubject: 'Inquiry',
  clientMessage: 'Hello, I have a question...',
});
```

**Request Payload (JSON):**
```json
{
  "clientName": "John Doe",
  "clientEmail": "john@example.com",
  "clientSubject": "General Inquiry",
  "clientMessage": "I'd like to know more about your services"
}
```

**Response:**
```json
true  // Boolean indicating success/failure
```

**AJAX Features Enabled:**
- ✅ Asynchronous requests (non-blocking UI)
- ✅ JSON serialization/deserialization
- ✅ CORS preflight handling (automatic OPTIONS request)
- ✅ Error handling and retry capability
- ✅ Credentials support for authentication

---

## 🚀 Getting Started

### Prerequisites
- Java 17+ installed
- Maven 3.9+
- Docker (for containerized deployment)
- MySQL credentials for AWS RDS
- Email service credentials (Gmail App Password or AWS SES)

### Build & Run Locally

```bash
# 1. Clone the repository
git clone https://github.com/nganeshm/ClientAuthApp.git
cd ClientAuthApp

# 2. Build the application
mvn clean package -DskipTests

# 3. Run locally (development)
mvn spring-boot:run

# 4. Test the API
curl -X POST http://localhost:8080/api/client/saveClient \
  -H "Content-Type: application/json" \
  -d '{
    "clientName": "Test User",
    "clientEmail": "test@example.com",
    "clientSubject": "Test",
    "clientMessage": "Testing the API"
  }'
```

### Docker Deployment

```bash
# 1. Build Docker image
docker build -t client-service:latest .

# 2. Run Docker container
docker run -d \
  --name client-service \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://clientdb.c7gccsuya4ux.ap-south-1.rds.amazonaws.com:3306/clientdb?useSSL=false" \
  -e SPRING_DATASOURCE_USERNAME="admin" \
  -e SPRING_DATASOURCE_PASSWORD="your-password" \
  -e MAIL_USERNAME="your-email@gmail.com" \
  -e MAIL_PASSWORD="your-app-password" \
  client-service:latest

# 3. View logs
docker logs -f client-service
```

### Environment Variables

Set these before running the application:

| Variable | Purpose | Example |
|----------|---------|---------|
| `SPRING_DATASOURCE_URL` | RDS MySQL connection string | `jdbc:mysql://clientdb.c7gccsuya4ux.ap-south-1.rds.amazonaws.com:3306/clientdb` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `admin` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `YourSecurePassword` |
| `MAIL_USERNAME` | Email sender address | `ganeshnarangle@gmail.com` |
| `MAIL_PASSWORD` | Email SMTP password | Gmail App Password or AWS SES credentials |
| `SERVER_PORT` | Application port (optional) | `8081` (default: 8080) |

---

## 📧 Email Configuration

### Supported Email Providers

1. **Gmail (Development)** ⭐ Recommended for testing
   - 500 emails/day limit
   - Requires App Password (not regular password)
   - Setup: https://myaccount.google.com/apppasswords

2. **AWS SES (Production)** ⭐ Recommended for AWS deployment
   - 62,000 free emails/month from EC2
   - Highly scalable and reliable
   - No egress charges within AWS

3. **SendGrid / Mailgun** - Alternative options

**Default Configuration (Gmail):**
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
```

---

## 📊 Database Schema

**Table: `client`**

| Column | Type | Constraints |
|--------|------|-------------|
| `clientId` | VARCHAR(36) | PRIMARY KEY (UUID) |
| `clientName` | VARCHAR(255) | NOT NULL, UNIQUE |
| `clientEmail` | VARCHAR(255) | NOT NULL |
| `subject` | VARCHAR(255) | NULL |
| `message` | TEXT | NULL |

**Auto-generated by Hibernate:** `ddl-auto: update` creates/updates schema on startup

---

## 🔒 Security Considerations

- ✅ **CORS whitelist:** Only allows requests from S3 origin
- ✅ **Credentials via environment variables:** Never hardcode in code
- ✅ **AWS RDS encryption:** Enable encryption at rest (recommended)
- ✅ **Security groups:** Restrict inbound/outbound traffic
- ✅ **Email failure isolation:** Email errors don't break registration
- ✅ **Validation:** Client name and email are required fields

### Production Recommendations

1. Use AWS Secrets Manager for storing sensitive credentials
2. Enable SSL/TLS for database connections
3. Implement rate limiting on API endpoints
4. Add request validation and sanitization
5. Enable audit logging for registration events
6. Use IAM roles instead of hardcoded credentials

---

## 📖 API Documentation

### POST /api/client/saveClient

**Description:** Register a new client and send welcome email

**Request:**
```
POST /api/client/saveClient
Content-Type: application/json
```

**Body:**
```json
{
  "clientName": "string (required)",
  "clientEmail": "string (required)",
  "clientSubject": "string (optional)",
  "clientMessage": "string (optional)"
}
```

**Response:**
```
200 OK
Content-Type: application/json

true  // Success
false // Failure (missing required fields)
```

**CORS:** ✅ Allowed from S3 origin

---

## 🐛 Troubleshooting

### Cannot connect to RDS
- Verify EC2 security group allows outbound on port 3306
- Verify RDS security group allows inbound from EC2 security group
- Test connection: `docker exec -it client-service mysql -h clientdb.c7gccsuya4ux.ap-south-1.rds.amazonaws.com -u admin -p`

### CORS errors from React frontend
- Verify S3 origin URL matches `CorsConfig.java`
- Check browser console for preflight OPTIONS request status
- Ensure backend is running and accessible

### Email not sending
- Verify SMTP credentials are correct
- Check outbound port 587 is allowed in security groups
- Review application logs: `docker logs client-service`
- For Gmail: Verify App Password is generated (not regular password)

### Port already in use
```bash
docker ps -a  # Check for existing containers
docker rm client-service  # Remove old container
```

---

## 📚 Additional Resources

- **Docker Deployment Guide:** See `DOCKER_DEPLOYMENT.md`
- **Email Setup Guide:** See `EMAIL_SETUP_GUIDE.md`
- **Spring Boot Docs:** https://docs.spring.io/spring-boot/
- **AWS RDS:** https://docs.aws.amazon.com/rds/
- **AWS SES:** https://docs.aws.amazon.com/ses/

---

## 📝 License

Open source project. See LICENSE file for details.

## 👤 Author

**Ganesh Madhavrao Narangle**  
Email: ganeshnarangle@gmail.com  
GitHub: [@nganeshm](https://github.com/nganeshm)

---

## 🤝 Contributing

Contributions are welcome! Please fork the repository and submit a pull request.

---

**Last Updated:** July 2026  
**Status:** Active Development
