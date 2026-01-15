package local.pandora.command;

import local.pandora.exception.PandoraException;
import local.pandora.storage.VaultPaths;
import picocli.CommandLine;
import local.pandora.crypto.KeyDerivation;
import local.pandora.storage.Vault;
import local.pandora.storage.VaultFile;

import javax.crypto.SecretKey;
import java.nio.file.Files;
import java.io.Console;
import java.util.Arrays;

import static java.lang.System.*;

@CommandLine.Command(name = "init", description = "Initialize a new vault")
public class InitCommand extends BaseVaultCommand {

    @Override
    public Integer call() {
        try {
            Console console = getConsoleOrFail();
            if (console == null) {
                return 1;
            }

            if (Files.exists(VaultPaths.PANDORA_FILE)) {
                err.println("Vault already exists! Initialization aborted.");
                err.println("If you forgot your password, there is no way to recover it.");
                return 1;
            }

            char[] masterPassword = console.readPassword("Enter master password: ");
            if (masterPassword == null) {
                return 1;
            }

            try {
                byte[] salt = KeyDerivation.generateSalt();
                SecretKey key = KeyDerivation.deriveKey(masterPassword, salt);

                Files.createDirectories(VaultPaths.PANDORA_DIR);

                Vault vault = new Vault();
                VaultFile.saveVault(vault, VaultPaths.PANDORA_FILE, key, salt);

                out.println("Vault initialized at vault.enc");
                return 0;
            } finally {
                Arrays.fill(masterPassword, '\0');
            }
        } catch (PandoraException e) {
            err.println("Error: " + e.getMessage());
            return 1;
        } catch (Exception e) {
            err.println("Unexpected error: " + e.getMessage());
            return 1;
        }
    }
}
