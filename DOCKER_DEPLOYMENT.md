# Docker Deployment Guide for Client Service

## Prerequisites
- Docker installed on your machine/EC2
- Maven to build the JAR file
- RDS security group configured to allow connections

---

## Build Commands

### Step 1: Build the JAR file
```bash
cd /home/ganeshn/Music/client
mvn clean package -DskipTests
```

This creates `target/client-0.0.1-SNAPSHOT.jar`

### Step 2: Build the Docker image
```bash
docker build -t client-service:latest .
```

### Step 3: Run the Docker container

#### Option A: Using environment variables (Recommended)
```bash
docker run -d \
  --name client-service \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://clientdb.c7gccsuya4ux.ap-south-1.rds.amazonaws.com:3306/clientdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=10000" \
  -e SPRING_DATASOURCE_USERNAME="admin" \
  -e SPRING_DATASOURCE_PASSWORD="GaneshAWS#2811" \
  client-service:latest
```

#### Option B: Using the embedded configuration (current setup)
```bash
docker run -d \
  --name client-service \
  -p 8081:8081 \
  client-service:latest
```

---

## Docker Commands Reference

### View running containers
```bash
docker ps
```

### View container logs
```bash
docker logs client-service
docker logs -f client-service  # Follow logs in real-time
```

### Stop the container
```bash
docker stop client-service
```

### Start the container
```bash
docker start client-service
```

### Remove the container
```bash
docker rm client-service
```

### Remove the image
```bash
docker rmi client-service:latest
```

### Execute commands inside running container
```bash
docker exec -it client-service bash
```

### View container resource usage
```bash
docker stats client-service
```

---

## Push to Docker Hub (Optional)

### Step 1: Tag the image
```bash
docker tag client-service:latest your-dockerhub-username/client-service:latest
```

### Step 2: Login to Docker Hub
```bash
docker login
```

### Step 3: Push the image
```bash
docker push your-dockerhub-username/client-service:latest
```

### Step 4: Pull and run on another machine
```bash
docker pull your-dockerhub-username/client-service:latest
docker run -d -p 8081:8081 --name client-service your-dockerhub-username/client-service:latest
```

---

## Deploy to AWS EC2 with Docker

### Step 1: Install Docker on EC2 (Amazon Linux 2)
```bash
sudo yum update -y
sudo yum install -y docker
sudo service docker start
sudo usermod -a -G docker ec2-user
# Logout and login again for group changes to take effect
```

### Step 2: Copy JAR to EC2
```bash
# From your local machine
scp -i ~/.ssh/your-key.pem target/client-0.0.1-SNAPSHOT.jar ec2-user@<EC2-PUBLIC-IP>:~/
scp -i ~/.ssh/your-key.pem Dockerfile ec2-user@<EC2-PUBLIC-IP>:~/
```

### Step 3: Build and run on EC2
```bash
# SSH into EC2
ssh -i ~/.ssh/your-key.pem ec2-user@<EC2-PUBLIC-IP>

# Build image
docker build -t client-service:latest .

# Run container
docker run -d \
  --name client-service \
  -p 8081:8081 \
  --restart unless-stopped \
  client-service:latest

# Check logs
docker logs -f client-service
```

### Step 4: Update EC2 Security Group
Allow inbound traffic on port 8081:
- Type: Custom TCP
- Port: 8081
- Source: 0.0.0.0/0 (or specific IP range)

---

## Deploy to AWS ECR (Elastic Container Registry)

### Step 1: Create ECR repository
```bash
aws ecr create-repository --repository-name client-service --region ap-south-1
```

### Step 2: Authenticate Docker to ECR
```bash
aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.ap-south-1.amazonaws.com
```

### Step 3: Tag and push image
```bash
docker tag client-service:latest <AWS_ACCOUNT_ID>.dkr.ecr.ap-south-1.amazonaws.com/client-service:latest
docker push <AWS_ACCOUNT_ID>.dkr.ecr.ap-south-1.amazonaws.com/client-service:latest
```

### Step 4: Pull and run from ECR
```bash
docker pull <AWS_ACCOUNT_ID>.dkr.ecr.ap-south-1.amazonaws.com/client-service:latest
docker run -d -p 8081:8081 --name client-service <AWS_ACCOUNT_ID>.dkr.ecr.ap-south-1.amazonaws.com/client-service:latest
```

---

## Docker Compose (Optional - for multi-container setup)

Create `docker-compose.yml`:
```yaml
version: '3.8'

services:
  client-service:
    image: client-service:latest
    container_name: client-service
    ports:
      - "8081:8081"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://clientdb.c7gccsuya4ux.ap-south-1.rds.amazonaws.com:3306/clientdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
      - SPRING_DATASOURCE_USERNAME=admin
      - SPRING_DATASOURCE_PASSWORD=GaneshAWS#2811
    restart: unless-stopped
```

Run with:
```bash
docker-compose up -d
docker-compose logs -f
docker-compose down
```

---

## Production Best Practices

### 1. Use Multi-Stage Build (Optimized Dockerfile)
Create `Dockerfile.multistage`:
```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/client-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]
```

Build with:
```bash
docker build -f Dockerfile.multistage -t client-service:latest .
```

### 2. Use Environment Variables (Never hardcode credentials)
Update `application.yaml`:
```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
```

### 3. Add Health Check
Add to Dockerfile:
```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1
```

### 4. Use AWS Secrets Manager
```bash
# Store secret
aws secretsmanager create-secret \
  --name client-service/db-password \
  --secret-string "GaneshAWS#2811" \
  --region ap-south-1

# Retrieve in application startup script
DB_PASSWORD=$(aws secretsmanager get-secret-value --secret-id client-service/db-password --query SecretString --output text)
```

### 5. Run as non-root user
Add to Dockerfile:
```dockerfile
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring
```

---

## Troubleshooting

### Container exits immediately
```bash
docker logs client-service
```

### Cannot connect to RDS
- Ensure EC2 security group allows outbound on port 3306
- Ensure RDS security group allows inbound from EC2 security group
- Test: `docker exec -it client-service ping clientdb.c7gccsuya4ux.ap-south-1.rds.amazonaws.com`

### Port already in use
```bash
sudo lsof -i :8081
docker ps -a  # Check if old container is still running
```

### Out of memory
```bash
docker run -d -p 8081:8081 -m 512m --memory-swap 512m client-service:latest
```

---

## Test the API

After container is running:
```bash
# Health check (if actuator is enabled)
curl http://localhost:8081/actuator/health

# Test your API endpoints
curl http://localhost:8081/clients
```

From your S3 website:
```javascript
fetch('http://<EC2-PUBLIC-IP>:8081/clients')
  .then(response => response.json())
  .then(data => console.log(data));
```

---

## Summary Checklist

- [x] Dockerfile created with correct port (8081)
- [x] .dockerignore created to optimize build
- [ ] Build JAR: `mvn clean package -DskipTests`
- [ ] Build Docker image: `docker build -t client-service:latest .`
- [ ] Run container: `docker run -d -p 8081:8081 --name client-service client-service:latest`
- [ ] Check logs: `docker logs -f client-service`
- [ ] Test API: `curl http://localhost:8081/clients`
- [ ] Update EC2 security group to allow port 8081
- [ ] Update CORS origin if needed

