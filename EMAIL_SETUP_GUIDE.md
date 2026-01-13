# Email Setup Guide - Spring Boot Client Service

## ✅ Implementation Complete

I've added email functionality to automatically send welcome emails when clients register.

---

## 📦 What Was Added

### 1. **Maven Dependency** (pom.xml)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

### 2. **Email Configuration** (application.yaml)
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME:your-email@gmail.com}
    password: ${MAIL_PASSWORD:your-app-password}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
```

### 3. **Email Service Interface** (EmailService.java)
```java
package service;

public interface EmailService {
    void sendWelcomeEmail(String toEmail, String clientName);
}
```

### 4. **Email Service Implementation** (EmailServiceImpl.java)
- Sends welcome emails using Spring's `JavaMailSender`
- Handles errors gracefully (won't break registration if email fails)
- Customizable email template

### 5. **Updated ClientServiceImpl**
- Automatically sends welcome email after successful registration
- Email sent asynchronously (doesn't block the registration process)

---

## 🔧 Email Provider Options

### Option 1: Gmail (Easy for Development) ⭐ Recommended for Testing

#### Step 1: Enable 2-Factor Authentication
1. Go to https://myaccount.google.com/security
2. Enable "2-Step Verification"

#### Step 2: Generate App Password
1. Go to https://myaccount.google.com/apppasswords
2. Select "Mail" and "Other (Custom name)"
3. Enter "Spring Boot Client App"
4. Click "Generate"
5. Copy the 16-character password (e.g., `abcd efgh ijkl mnop`)

#### Step 3: Configure application.yaml
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: abcdefghijklmnop  # 16-char app password (no spaces)
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

#### ⚠️ Gmail Limitations:
- 500 emails/day for free accounts
- 2000 emails/day for Google Workspace
- Not recommended for production

---

### Option 2: AWS SES (Best for Production) ⭐ Recommended for AWS EC2

#### Advantages:
- ✅ Cost-effective ($0.10 per 1000 emails)
- ✅ Highly scalable
- ✅ Reliable delivery
- ✅ Works perfectly with EC2

#### Setup Steps:

**Step 1: Verify Your Email (Sender Identity)**
```bash
# AWS Console → SES → Verified identities → Create identity
# Choose: Email address
# Enter: your-noreply@yourdomain.com
# Check inbox and verify
```

Or via AWS CLI:
```bash
aws ses verify-email-identity --email-address your-email@example.com --region ap-south-1
```

**Step 2: Create SMTP Credentials**
```bash
# AWS Console → SES → SMTP Settings
# Create SMTP credentials
# Save: SMTP Username and SMTP Password
```

**Step 3: Update application.yaml**
```yaml
spring:
  mail:
    host: email-smtp.ap-south-1.amazonaws.com
    port: 587
    username: YOUR_SMTP_USERNAME
    password: YOUR_SMTP_PASSWORD
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
```

**Step 4: Request Production Access (if needed)**
- By default, SES is in "Sandbox mode" (can only send to verified emails)
- To send to any email: AWS Console → SES → Account Dashboard → Request production access

**Step 5: Set Environment Variables on EC2**
```bash
export MAIL_USERNAME=your-smtp-username
export MAIL_PASSWORD=your-smtp-password
java -jar client-0.0.1-SNAPSHOT.jar
```

---

### Option 3: SendGrid (Easy API, Good Free Tier)

#### Advantages:
- ✅ 100 emails/day free
- ✅ Easy setup
- ✅ Great documentation

#### Setup:
1. Sign up at https://sendgrid.com
2. Create API Key (Settings → API Keys)
3. Update application.yaml:

```yaml
spring:
  mail:
    host: smtp.sendgrid.net
    port: 587
    username: apikey
    password: YOUR_SENDGRID_API_KEY
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

---

### Option 4: Mailgun

```yaml
spring:
  mail:
    host: smtp.mailgun.org
    port: 587
    username: postmaster@your-domain.mailgun.org
    password: YOUR_MAILGUN_PASSWORD
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

---

## 🚀 How to Use (After Setup)

### 1. Resolve Maven Dependencies

Run this command to download the Spring Mail library:
```bash
cd /home/ganeshn/Music/client
mvn clean install -DskipTests
```

Or in IntelliJ IDEA:
- Right-click on `pom.xml` → Maven → Reload Project

### 2. Configure Email Credentials

**For Development (using environment variables):**
```bash
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
mvn spring-boot:run
```

**For Production (Docker):**
```bash
docker run -d \
  --name client-service \
  -p 8081:8081 \
  -e MAIL_USERNAME=your-email@gmail.com \
  -e MAIL_PASSWORD=your-app-password \
  client-service:latest
```

**For Production (EC2 systemd):**
Edit `/etc/systemd/system/client.service`:
```ini
[Service]
Environment=MAIL_USERNAME=your-email@gmail.com
Environment=MAIL_PASSWORD=your-app-password
```

### 3. Test Email Functionality

**Register a new client via API:**
```bash
curl -X POST http://localhost:8081/clients \
  -H "Content-Type: application/json" \
  -d '{
    "clientName": "Test User",
    "clientEmail": "test@example.com",
    "clientSubject": "Test Subject",
    "clientMessage": "Test Message"
  }'
```

**Check logs for email status:**
```bash
✓ Welcome email sent successfully to: test@example.com
```

Or if error:
```bash
✗ Failed to send email to: test@example.com
Error: Authentication failed
```

---

## 🎨 Customize Email Template

Edit `EmailServiceImpl.java` → `buildEmailBody()` method:

```java
private String buildEmailBody(String clientName) {
    return String.format(
        "Dear %s,\n\n" +
        "🎉 Welcome to Our Service!\n\n" +
        "Thank you for registering. We're excited to have you on board!\n\n" +
        "Your registration details:\n" +
        "- Name: %s\n" +
        "- Registration Date: %s\n\n" +
        "Need help? Contact us at support@example.com\n\n" +
        "Best regards,\n" +
        "The Client Service Team",
        clientName,
        clientName,
        java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    );
}
```

---

## 📧 Send HTML Emails (Advanced)

To send HTML emails instead of plain text:

### 1. Update EmailServiceImpl.java:

```java
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;

@Override
public void sendWelcomeEmail(String toEmail, String clientName) {
    try {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Welcome to Our Service! 🎉");
        helper.setText(buildHtmlEmailBody(clientName), true); // true = HTML
        
        mailSender.send(message);
        System.out.println("✓ Welcome email sent to: " + toEmail);
    } catch (Exception e) {
        System.err.println("✗ Failed to send email: " + e.getMessage());
    }
}

private String buildHtmlEmailBody(String clientName) {
    return String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: #4CAF50; color: white; padding: 20px; text-align: center; }
                .content { padding: 20px; background: #f9f9f9; }
                .footer { text-align: center; padding: 10px; font-size: 12px; color: #666; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>🎉 Welcome!</h1>
                </div>
                <div class="content">
                    <p>Dear <strong>%s</strong>,</p>
                    <p>Thank you for registering with us!</p>
                    <p>We're excited to have you on board.</p>
                </div>
                <div class="footer">
                    <p>This is an automated message. Please do not reply.</p>
                </div>
            </div>
        </body>
        </html>
        """, clientName);
}
```

---

## 🔒 Security Best Practices

### 1. Never Commit Credentials
Add to `.gitignore`:
```
application-local.yaml
application-prod.yaml
*.env
```

### 2. Use Environment Variables
```yaml
spring:
  mail:
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
```

### 3. Use AWS Secrets Manager (Production)
```bash
# Store secret
aws secretsmanager create-secret \
  --name client-service/mail-password \
  --secret-string "your-password"

# Retrieve in startup script
MAIL_PASSWORD=$(aws secretsmanager get-secret-value \
  --secret-id client-service/mail-password \
  --query SecretString --output text)
```

### 4. Rotate Credentials Regularly
- Change app passwords every 90 days
- Use IAM roles for AWS SES when possible

---

## 🐛 Troubleshooting

### Error: "Cannot resolve symbol 'SimpleMailMessage'"

**Solution 1: Reload Maven Dependencies**
```bash
mvn clean install -DskipTests
```

**Solution 2: IntelliJ IDEA**
- Right-click `pom.xml` → Maven → Reload Project
- File → Invalidate Caches / Restart

**Solution 3: Verify Dependency**
Check `pom.xml` has:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

---

### Error: "Authentication failed"

**Cause**: Wrong credentials or app password not generated

**Solution**:
1. Verify email/password are correct
2. For Gmail: Use App Password (not regular password)
3. Check if 2FA is enabled on Gmail
4. Test SMTP credentials:
```bash
curl -v --url 'smtp://smtp.gmail.com:587' \
  --mail-from 'your-email@gmail.com' \
  --mail-rcpt 'recipient@example.com' \
  --user 'your-email@gmail.com:your-app-password'
```

---

### Error: "Mail server connection failed"

**Cause**: Firewall blocking port 587 or wrong SMTP host

**Solution**:
1. Test port connectivity:
```bash
telnet smtp.gmail.com 587
# OR
nc -zv smtp.gmail.com 587
```

2. Check EC2 security group allows outbound on port 587
3. Verify SMTP host is correct for your provider

---

### Error: "Could not send email: 554 Message rejected"

**Cause**: Email marked as spam or sender not verified

**Solution**:
- For AWS SES: Verify sender email in SES console
- For Gmail: Check if account is marked for suspicious activity
- Add SPF/DKIM records to your domain

---

## 📊 Email Sending Comparison

| Provider | Free Tier | Cost | Setup Difficulty | Best For |
|----------|-----------|------|------------------|----------|
| **Gmail** | 500/day | Free | ⭐ Easy | Development/Testing |
| **AWS SES** | 62,000/month (from EC2) | $0.10/1000 | ⭐⭐ Medium | Production (AWS) |
| **SendGrid** | 100/day | $19.95/month | ⭐ Easy | Startups |
| **Mailgun** | 100/day | $35/month | ⭐⭐ Medium | Enterprise |

---

## 🎯 Recommended Setup for Your Project

Since you're deploying on **AWS EC2 with RDS**, I recommend:

### **Use AWS SES** ✅

**Why?**
- Already on AWS infrastructure
- 62,000 free emails/month when sending from EC2
- No egress charges within AWS
- Highly reliable and scalable

**Quick Setup:**
```bash
# 1. Verify email in SES
aws ses verify-email-identity --email-address noreply@yourdomain.com --region ap-south-1

# 2. Create SMTP credentials (AWS Console → SES → SMTP Settings)

# 3. Update application.yaml
spring:
  mail:
    host: email-smtp.ap-south-1.amazonaws.com
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}

# 4. Set environment variables in Docker/systemd
MAIL_USERNAME=your-ses-smtp-username
MAIL_PASSWORD=your-ses-smtp-password
```

---

## ✅ Summary Checklist

- [x] Added `spring-boot-starter-mail` dependency to pom.xml
- [x] Created `EmailService` interface
- [x] Created `EmailServiceImpl` with welcome email logic
- [x] Updated `ClientServiceImpl` to send emails after registration
- [x] Added email configuration to application.yaml
- [ ] Run `mvn clean install` to download dependencies
- [ ] Choose email provider (Gmail/AWS SES/SendGrid)
- [ ] Generate SMTP credentials for chosen provider
- [ ] Update `MAIL_USERNAME` and `MAIL_PASSWORD` in application.yaml or environment
- [ ] Test email sending with a client registration
- [ ] Verify email arrives in inbox (check spam folder)
- [ ] Update email template as needed
- [ ] Configure production credentials (AWS Secrets Manager)

---

## 📞 Need Help?

If you still see "Cannot resolve SimpleMailMessage" after `mvn clean install`:
1. Check your IDE is using the correct JDK (Java 17)
2. Refresh/reimport Maven project in your IDE
3. Restart your IDE
4. Verify internet connection (Maven needs to download dependencies)

---

## 🚀 Next Steps

1. **Reload Maven dependencies** to resolve the import errors
2. **Choose and configure** an email provider (Gmail for testing, AWS SES for production)
3. **Test** the welcome email by registering a new client
4. **Customize** the email template to match your brand
5. **Add more email types** (password reset, notifications, etc.)

