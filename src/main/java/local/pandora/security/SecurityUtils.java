package local.pandora.security;

import local.pandora.exception.PandoraException;
import local.pandora.logging.VaultLogger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Pattern;

public class SecurityUtils {
    
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final long LOCKOUT_DURATION_MS = 5 * 60 * 1000; // 5 minutes
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,}$"
    );
    
    private static int failedAttempts = 0;
    private static long lockoutUntil = 0;
    
    private SecurityUtils() {}
    
    public static boolean isLockedOut() {
        return System.currentTimeMillis() < lockoutUntil;
    }
    
    public static long getRemainingLockoutTime() {
        return Math.max(0, lockoutUntil - System.currentTimeMillis());
    }
    
    public static void recordFailedAttempt() {
        failedAttempts++;
        VaultLogger.logAuthenticationAttempt(false);
        
        if (failedAttempts >= MAX_LOGIN_ATTEMPTS) {
            lockoutUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS;
            VaultLogger.security("Account locked due to too many failed attempts");
        }
    }
    
    public static void recordSuccessfulAttempt() {
        failedAttempts = 0;
        lockoutUntil = 0;
        VaultLogger.logAuthenticationAttempt(true);
    }
    
    public static String hashPassword(char[] password) throws PandoraException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(new String(password).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new PandoraException("Failed to hash password", e);
        }
    }
    
    public static boolean validatePasswordStrength(char[] password) {
        String passwordStr = new String(password);
        return STRONG_PASSWORD_PATTERN.matcher(passwordStr).matches();
    }
    
    public static String getPasswordStrengthDescription(char[] password) {
        String passwordStr = new String(password);
        
        if (passwordStr.length() < 8) {
            return "Very Weak - Too short";
        } else if (passwordStr.length() < 12) {
            return "Weak - Should be at least 12 characters";
        } else if (!STRONG_PASSWORD_PATTERN.matcher(passwordStr).matches()) {
            return "Moderate - Missing uppercase, lowercase, number, or special character";
        } else if (passwordStr.length() < 16) {
            return "Good";
        } else {
            return "Excellent";
        }
    }
    
    public static String generatePassword(int length, boolean includeUppercase, 
                                     boolean includeLowercase, boolean includeNumbers, 
                                     boolean includeSpecialChars) {
        StringBuilder chars = new StringBuilder();
        
        if (includeLowercase) chars.append("abcdefghijklmnopqrstuvwxyz");
        if (includeUppercase) chars.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        if (includeNumbers) chars.append("0123456789");
        if (includeSpecialChars) chars.append("!@#$%^&*()_+-=[]{}|;:,.<>?");
        
        if (chars.isEmpty()) {
            throw new IllegalArgumentException("At least one character type must be selected");
        }
        
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        
        return password.toString();
    }
    
    public static String generateStrongPassword() {
        return generatePassword(16, true, true, true, true);
    }
    
    public static void secureClear(char[] array) {
        if (array != null) {
            Arrays.fill(array, '\0');
        }
    }
    
    public static void secureClear(byte[] array) {
        if (array != null) {
            Arrays.fill(array, (byte) 0);
        }
    }
    
    public static String maskSensitiveData(String data) {
        if (data == null || data.length() <= 4) {
            return "****";
        }
        return data.substring(0, 2) + "****" + data.substring(data.length() - 2);
    }
    
    public static boolean isValidEntryName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        // Allow alphanumeric, spaces, hyphens, underscores, and dots
        return Pattern.matches("^[a-zA-Z0-9 ._-]+$", name);
    }
    
    public static String sanitizeEntryName(String name) {
        if (name == null) {
            return "";
        }
        
        // Remove potentially dangerous characters
        return name.replaceAll("[^a-zA-Z0-9 ._-]", "_");
    }
    
    public static void checkMemoryLeak() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        long maxMemory = runtime.maxMemory();
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        if (memoryUsagePercent > 80) {
            VaultLogger.warn("High memory usage detected: " + String.format("%.1f%%", memoryUsagePercent));
        }
    }
    
    public static class PasswordValidator {
        private final boolean hasLowercase;
        private final boolean hasUppercase;
        private final boolean hasNumbers;
        private final boolean hasSpecialChars;
        private final boolean hasValidLength;
        
        public PasswordValidator(String password) {
            this.hasLowercase = password.matches(".*[a-z].*");
            this.hasUppercase = password.matches(".*[A-Z].*");
            this.hasNumbers = password.matches(".*\\d.*");
            this.hasSpecialChars = password.matches(".*[@$!%*?&].*");
            this.hasValidLength = password.length() >= 12;
        }
        
        public boolean isValid() {
            return hasLowercase && hasUppercase && hasNumbers && hasSpecialChars && hasValidLength;
        }
        
        public String getValidationMessage() {
            StringBuilder message = new StringBuilder();
            if (!hasValidLength) message.append("Must be at least 12 characters. ");
            if (!hasLowercase) message.append("Must contain lowercase letters. ");
            if (!hasUppercase) message.append("Must contain uppercase letters. ");
            if (!hasNumbers) message.append("Must contain numbers. ");
            if (!hasSpecialChars) message.append("Must contain special characters. ");
            return message.toString().trim();
        }
    }
}
