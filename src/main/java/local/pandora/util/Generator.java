package local.pandora.util;

import java.security.SecureRandom;

public class Generator {

    private Generator() {}

    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateRandomString(int length) {
        validateLength(length);
        
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = RANDOM.nextInt(ALPHANUMERIC_CHARS.length());
            sb.append(ALPHANUMERIC_CHARS.charAt(idx));
        }
        return sb.toString();
    }

    public static String generateSecureRandomString(int length) {
        validateLength(length);
        
        byte[] randomBytes = new byte[length];
        RANDOM.nextBytes(randomBytes);
        
        StringBuilder sb = new StringBuilder(length);
        for (byte b : randomBytes) {
            sb.append(ALPHANUMERIC_CHARS.charAt(Math.abs(b % ALPHANUMERIC_CHARS.length())));
        }
        return sb.toString();
    }

    private static void validateLength(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        if (length > 1000) {
            throw new IllegalArgumentException("Length too large (max 1000)");
        }
    }
}
