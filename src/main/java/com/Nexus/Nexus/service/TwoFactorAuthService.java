package com.Nexus.Nexus.service;

import com.Nexus.Nexus.model.TwoFactorAuth;
import com.Nexus.Nexus.repository.TwoFactorAuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
// import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TwoFactorAuthService {
    
    @Autowired
    private TwoFactorAuthRepository twoFactorAuthRepository;
    
    // @Autowired
    // private JavaMailSender mailSender;
    
    @Value("${app.2fa.otp-expiry-minutes:5}")
    private int otpExpiryMinutes;
    
    @Value("${app.2fa.otp-length:6}")
    private int otpLength;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Generates and sends OTP for 2FA
     * @param userId User ID
     * @param email User email
     * @param purpose Purpose (LOGIN, REGISTER, PASSWORD_RESET)
     * @return Generated OTP (for testing purposes, in production this should not be returned)
     */
    public String generateAndSendOTP(String userId, String email, String purpose) {
        // Delete any existing OTP for this user and purpose
        twoFactorAuthRepository.deleteByUserIdAndPurpose(userId, purpose);
        
        // Generate new OTP
        String otp = generateOTP();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpiryMinutes);
        
        // Save OTP to database
        TwoFactorAuth twoFactorAuth = new TwoFactorAuth(userId, email, otp, expiresAt, purpose);
        twoFactorAuthRepository.save(twoFactorAuth);
        
        // Send OTP via email
        sendOTPEmail(email, otp, purpose);
        
        // Return OTP for testing (remove in production)
        return otp;
    }
    
    /**
     * Verifies the provided OTP
     * @param email User email
     * @param otp Provided OTP
     * @param purpose Purpose
     * @return true if OTP is valid, false otherwise
     */
    public boolean verifyOTP(String email, String otp, String purpose) {
        Optional<TwoFactorAuth> twoFactorAuthOpt = twoFactorAuthRepository
                .findByEmailAndOtpAndPurposeAndVerifiedFalse(email, otp, purpose);
        
        if (twoFactorAuthOpt.isEmpty()) {
            return false;
        }
        
        TwoFactorAuth twoFactorAuth = twoFactorAuthOpt.get();
        
        // Check if expired
        if (twoFactorAuth.isExpired()) {
            return false;
        }
        
        // Check max attempts
        if (twoFactorAuth.isMaxAttemptsReached()) {
            return false;
        }
        
        // Increment attempts
        twoFactorAuth.setAttempts(twoFactorAuth.getAttempts() + 1);
        
        // If OTP matches, mark as verified
        if (otp.equals(twoFactorAuth.getOtp())) {
            twoFactorAuth.setVerified(true);
            twoFactorAuthRepository.save(twoFactorAuth);
            return true;
        }
        
        // Save incremented attempts
        twoFactorAuthRepository.save(twoFactorAuth);
        return false;
    }
    
    /**
     * Checks if user has pending OTP verification
     * @param userId User ID
     * @param purpose Purpose
     * @return true if pending verification exists
     */
    public boolean hasPendingOTP(String userId, String purpose) {
        Optional<TwoFactorAuth> twoFactorAuth = twoFactorAuthRepository
                .findByUserIdAndPurposeAndVerifiedFalse(userId, purpose);
        
        if (twoFactorAuth.isPresent()) {
            TwoFactorAuth auth = twoFactorAuth.get();
            return !auth.isExpired() && !auth.isMaxAttemptsReached();
        }
        
        return false;
    }
    
    /**
     * Generates a random OTP
     */
    private String generateOTP() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }
    
    /**
     * Sends OTP via email (mock implementation)
     */
    private void sendOTPEmail(String toEmail, String otp, String purpose) {
        try {
            // Mock email sending - in production, use JavaMailSender
            String emailBody = buildEmailBody(otp, purpose);
            
            // Log the email content for testing purposes
            System.out.println("=== MOCK EMAIL SENT ===");
            System.out.println("To: " + toEmail);
            System.out.println("Subject: Nexus - Your Verification Code");
            System.out.println("Body: " + emailBody);
            System.out.println("=====================");
            
            // In production, uncomment the following:
            // SimpleMailMessage message = new SimpleMailMessage();
            // message.setFrom(fromEmail);
            // message.setTo(toEmail);
            // message.setSubject("Nexus - Your Verification Code");
            // message.setText(emailBody);
            // mailSender.send(message);
            
        } catch (Exception e) {
            // Log error but don't fail the operation
            System.err.println("Failed to send OTP email: " + e.getMessage());
        }
    }
    
    /**
     * Builds email body based on purpose
     */
    private String buildEmailBody(String otp, String purpose) {
        String action;
        switch (purpose.toUpperCase()) {
            case "LOGIN":
                action = "login to your account";
                break;
            case "REGISTER":
                action = "complete your registration";
                break;
            case "PASSWORD_RESET":
                action = "reset your password";
                break;
            default:
                action = "verify your identity";
        }
        
        return String.format(
                "Dear User,\n\n" +
                "Your verification code to %s is: %s\n\n" +
                "This code will expire in %d minutes.\n\n" +
                "If you didn't request this, please ignore this email.\n\n" +
                "Best regards,\n" +
                "The Nexus Team",
                action, otp, otpExpiryMinutes
        );
    }
    
    /**
     * Cleanup expired OTPs (should be called periodically)
     */
    public void cleanupExpiredOTPs() {
        twoFactorAuthRepository.deleteByExpiresAtBeforeAndVerifiedFalse(LocalDateTime.now());
    }
}