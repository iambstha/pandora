package local.pandora.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import local.pandora.crypto.CryptoUtils;
import local.pandora.exception.InvalidMasterPasswordException;
import local.pandora.exception.PandoraException;

import javax.crypto.AEADBadTagException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class VaultFile {

    private VaultFile() {}

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void saveVault(Vault vault, Path path, SecretKey key, byte[] salt) throws PandoraException {
        try {
            validateInputs(vault, path, key, salt);
            
            String json = mapper.writeValueAsString(vault);
            String encrypted = CryptoUtils.encrypt(json, key);
            VaultContainer container = new VaultContainer(Base64.getEncoder().encodeToString(salt), encrypted);
            String containerJson = mapper.writeValueAsString(container);
            
            Files.createDirectories(path.getParent());
            Files.writeString(path, containerJson);
        } catch (Exception e) {
            throw new PandoraException("Failed to save vault file", e);
        }
    }

    public static VaultContainer loadVaultContainer(Path path) throws PandoraException {
        try {
            validatePath(path);
            String containerJson = Files.readString(path);
            return mapper.readValue(containerJson, VaultContainer.class);
        } catch (IOException e) {
            throw new PandoraException("Failed to read vault file: " + path, e);
        } catch (Exception e) {
            throw new PandoraException("Failed to parse vault file", e);
        }
    }

    public static Vault decryptVault(VaultContainer container, SecretKey key) throws PandoraException {
        try {
            validateContainer(container);
            String decryptedJson = CryptoUtils.decrypt(container.getEncryptedData(), key);
            return mapper.readValue(decryptedJson, Vault.class);
        } catch (Exception e) {
            throw new PandoraException("Failed to decrypt vault file", e);
        }
    }

    private static void validateInputs(Vault vault, Path path, SecretKey key, byte[] salt) throws PandoraException {
        if (vault == null) {
            throw new PandoraException("Vault cannot be null");
        }
        if (path == null) {
            throw new PandoraException("Path cannot be null");
        }
        if (key == null) {
            throw new PandoraException("Key cannot be null");
        }
        if (salt == null || salt.length == 0) {
            throw new PandoraException("Salt cannot be null or empty");
        }
    }

    private static void validatePath(Path path) throws PandoraException {
        if (path == null) {
            throw new PandoraException("Path cannot be null");
        }
        if (!Files.exists(path)) {
            throw new PandoraException("Vault file does not exist: " + path);
        }
        if (!Files.isRegularFile(path)) {
            throw new PandoraException("Path is not a regular file: " + path);
        }
    }

    private static void validateContainer(VaultContainer container) throws PandoraException {
        if (container == null) {
            throw new PandoraException("VaultContainer cannot be null");
        }
        if (container.getSalt() == null || container.getSalt().trim().isEmpty()) {
            throw new PandoraException("Container salt cannot be null or empty");
        }
        if (container.getEncryptedData() == null || container.getEncryptedData().trim().isEmpty()) {
            throw new PandoraException("Container encrypted data cannot be null or empty");
        }
    }
}
