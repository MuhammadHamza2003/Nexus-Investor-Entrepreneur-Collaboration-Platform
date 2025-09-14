package com.Nexus.Nexus.service;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class InputSanitizationService {
    
    // Common patterns for validation and sanitization
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("(?i)<script[^>]*>.*?</script>");
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile("(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript|vbscript|onload|onerror|onclick)");
    private static final Pattern XSS_PATTERN = Pattern.compile("(?i)(javascript:|vbscript:|onload|onerror|onclick|onmouseover|onfocus|onblur|onchange|onsubmit)");
    
    /**
     * Sanitizes input by removing potentially harmful content
     * @param input The input string to sanitize
     * @return Sanitized string
     */
    public String sanitizeInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }
        
        String sanitized = input;
        
        // Remove script tags
        sanitized = SCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        
        // Remove HTML tags (basic)
        sanitized = HTML_TAG_PATTERN.matcher(sanitized).replaceAll("");
        
        // Remove potential XSS content
        sanitized = XSS_PATTERN.matcher(sanitized).replaceAll("");
        
        // Trim whitespace
        sanitized = sanitized.trim();
        
        return sanitized;
    }
    
    /**
     * Validates input for potential SQL injection patterns
     * @param input The input to check
     * @return true if input appears safe, false if potentially malicious
     */
    public boolean isInputSafe(String input) {
        if (input == null) {
            return true;
        }
        
        // Check for SQL injection patterns
        if (SQL_INJECTION_PATTERN.matcher(input).find()) {
            return false;
        }
        
        // Check for XSS patterns
        if (XSS_PATTERN.matcher(input).find()) {
            return false;
        }
        
        // Check for script tags
        if (SCRIPT_PATTERN.matcher(input).find()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Sanitizes and validates input, throwing exception if unsafe
     * @param input Input to sanitize and validate
     * @param fieldName Name of the field for error messages
     * @return Sanitized input
     * @throws IllegalArgumentException if input is unsafe
     */
    public String sanitizeAndValidate(String input, String fieldName) {
        if (!isInputSafe(input)) {
            throw new IllegalArgumentException("Invalid input detected in field: " + fieldName);
        }
        
        return sanitizeInput(input);
    }
    
    /**
     * Validates email format
     * @param email Email to validate
     * @return true if email format is valid
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return Pattern.matches(emailPattern, email.trim());
    }
    
    /**
     * Validates URL format
     * @param url URL to validate
     * @return true if URL format is valid
     */
    public boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return true; // Allow empty URLs
        }
        
        String urlPattern = "^https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$";
        return Pattern.matches(urlPattern, url.trim());
    }
    
    /**
     * Validates phone number format (international)
     * @param phoneNumber Phone number to validate
     * @return true if phone number format is valid
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return true; // Allow empty phone numbers
        }
        
        String phonePattern = "^[+]?[1-9]\\d{1,14}$";
        return Pattern.matches(phonePattern, phoneNumber.trim());
    }
}