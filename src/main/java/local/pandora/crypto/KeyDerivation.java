package local.pandora.crypto;

import local.pandora.exception.PandoraException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class KeyDerivation {

    private KeyDerivation() {}

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256; // bits
    private static final int SALT_LENGTH = 16; // bytes
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String KEY_ALGORITHM = "AES";

    public static SecretKey deriveKey(char[] password, byte[] salt) throws Exception {
        validateInputs(password, salt);
        
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] keyBytes = skf.generateSecret(spec).getEncoded();
            return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new PandoraException("Algorithm not available: " + ALGORITHM, e);
        } finally {
            spec.clearPassword();
        }
    }

    public static byte[] generateSalt() throws NoSuchAlgorithmException {
        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom.getInstanceStrong().nextBytes(salt);
        return salt;
    }

    public static String encodeSalt(byte[] salt) {
        if (salt == null || salt.length == 0) {
            throw new IllegalArgumentException("Salt cannot be null or empty");
        }
        return Base64.getEncoder().encodeToString(salt);
    }

    public static byte[] decodeSalt(String saltStr) {
        if (saltStr == null || saltStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Salt string cannot be null or empty");
        }
        return Base64.getDecoder().decode(saltStr);
    }

    private static void validateInputs(char[] password, byte[] salt) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (salt == null || salt.length == 0) {
            throw new IllegalArgumentException("Salt cannot be null or empty");
        }
        if (salt.length < SALT_LENGTH) {
            throw new IllegalArgumentException("Salt must be at least " + SALT_LENGTH + " bytes");
        }
    }
}
