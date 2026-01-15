package local.pandora.command;

import local.pandora.crypto.KeyDerivation;
import local.pandora.exception.InvalidMasterPasswordException;
import local.pandora.exception.PandoraException;
import local.pandora.logging.VaultLogger;
import local.pandora.security.SecurityUtils;
import local.pandora.storage.Vault;
import local.pandora.storage.VaultContainer;
import local.pandora.storage.VaultFile;
import local.pandora.storage.VaultPaths;

import javax.crypto.SecretKey;
import java.io.Console;
import java.nio.file.Files;
import java.util.Base64;
import java.util.concurrent.Callable;

import static java.lang.System.*;

public abstract class BaseVaultCommand implements Callable<Integer> {

    protected Console getConsoleOrFail() {
        Console console = console();
        if (console == null) {
            err.println("No console available. Run this from a real terminal.");
            return null;
        }
        return console;
    }

    protected boolean validateVaultExists() {
        if (!Files.exists(VaultPaths.PANDORA_FILE)) {
            err.println("Pandora is not initialized. Run: pandora init");
            return false;
        }
        return true;
    }

    protected VaultOperationResult authenticateAndLoadVault() {
        Console console = getConsoleOrFail();
        if (console == null) {
            return null;
        }

        if (!validateVaultExists()) {
            return null;
        }

        // Check for account lockout
        if (SecurityUtils.isLockedOut()) {
            long remainingMinutes = SecurityUtils.getRemainingLockoutTime() / 1000 / 60;
            err.println("Account is locked. Try again in " + remainingMinutes + " minutes.");
            return null;
        }

        char[] masterPassword = console.readPassword("Enter master password: ");
        if (masterPassword == null) {
            return null;
        }

        try {
            VaultLogger.logVaultAccess("Authentication attempt");
            
            VaultContainer container = VaultFile.loadVaultContainer(VaultPaths.PANDORA_FILE);
            byte[] salt = Base64.getDecoder().decode(container.getSalt());
            SecretKey key = KeyDerivation.deriveKey(masterPassword, salt);
            
            Vault vault;
            try {
                vault = VaultFile.decryptVault(container, key);
                SecurityUtils.recordSuccessfulAttempt();
                VaultLogger.info("Authentication successful");
            } catch (InvalidMasterPasswordException e) {
                SecurityUtils.recordFailedAttempt();
                VaultLogger.warn("Authentication failed: " + e.getMessage());
                err.println(e.getMessage());
                return null;
            }

            return new VaultOperationResult(vault, key, salt);
        } catch (PandoraException e) {
            VaultLogger.error("Vault operation failed", e);
            err.println("Error: " + e.getMessage());
            return null;
        } catch (Exception e) {
            VaultLogger.error("Unexpected error during authentication", e);
            err.println("Unexpected error during authentication: " + e.getMessage());
            return null;
        } finally {
            SecurityUtils.secureClear(masterPassword);
        }
    }

    protected static class VaultOperationResult {
        private final Vault vault;
        private final SecretKey key;
        private final byte[] salt;

        public VaultOperationResult(Vault vault, SecretKey key, byte[] salt) {
            this.vault = vault;
            this.key = key;
            this.salt = salt;
        }

        public Vault getVault() {
            return vault;
        }

        public SecretKey getKey() {
            return key;
        }

        public byte[] getSalt() {
            return salt;
        }
    }
}
