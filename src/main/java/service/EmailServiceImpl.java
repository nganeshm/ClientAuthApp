package service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String clientName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Our Service! 🎉");
            message.setText(buildEmailBody(clientName));

            mailSender.send(message);
            System.out.println("✓ Welcome email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("✗ Failed to send email to: " + toEmail);
            System.err.println("Error: " + e.getMessage());
            // Don't throw exception - we don't want email failure to break registration
        }
    }

    private String buildEmailBody(String clientName) {
        return String.format(
            "Dear %s,\n\n" +
            "Thank you for registering with us! 🎉\n\n" +
            "We're excited to have you on board. Your registration has been successfully completed.\n\n" +
            "If you have any questions or need assistance, feel free to reach out to us.\n\n" +
            "Best regards,\n" +
            "Ganesh Madhavrao Narangle\n\n" +
            "---\n" +
            "This is an automated message. Please do not reply to this email.",
            clientName
        );
    }
}

