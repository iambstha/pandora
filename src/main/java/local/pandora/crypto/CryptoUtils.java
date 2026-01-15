package local.pandora.crypto;

import local.pandora.exception.PandoraException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptoUtils {

    private CryptoUtils() {}

    private static final int AES_KEY_SIZE = 256; // bits
    private static final int GCM_IV_LENGTH = 12; // bytes (recommended)
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final String AES_GCM_ALGORITHM = "AES/GCM/NoPadding";
    private static final String AES_ALGORITHM = "AES";

    public static SecretKey generateAesKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(AES_KEY_SIZE, SecureRandom.getInstanceStrong());
        return keyGen.generateKey();
    }

    public static String encrypt(String plainText, SecretKey key) {
        validateInputs(plainText, key);
        
        try {
            byte[] iv = generateIv();
            Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes());

            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new PandoraException("Failed to encrypt data", e);
        }
    }

    public static String decrypt(String cipherText, SecretKey key) {
        validateInputs(cipherText, key);
        
        try {
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            
            if (decoded.length < GCM_IV_LENGTH) {
                throw new IllegalArgumentException("Invalid ciphertext length");
            }

            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[decoded.length - GCM_IV_LENGTH];
            
            // Extract IV from the beginning of ciphertext
            System.arraycopy(decoded, 0, iv, 0, GCM_IV_LENGTH);
            
            Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            
            // Extract encrypted data (after IV)
            System.arraycopy(decoded, GCM_IV_LENGTH, encrypted, 0, encrypted.length);
            
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted);
        } catch (Exception e) {
            throw new PandoraException("Failed to decrypt data", e);
        }
    }

    private static byte[] generateIv() throws NoSuchAlgorithmException {
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom.getInstanceStrong().nextBytes(iv);
        return iv;
    }

    private static void validateInputs(String text, SecretKey key) {
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
    }
}
