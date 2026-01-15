package local.pandora.command;

import local.pandora.backup.VaultBackup;
import local.pandora.config.VaultConfig;
import local.pandora.exception.PandoraException;
import local.pandora.logging.VaultLogger;
import local.pandora.security.SecurityUtils;
import local.pandora.storage.VaultPaths;
import local.pandora.storage.VaultFile;
import picocli.CommandLine;

import java.io.Console;

import static java.lang.System.err;
import static java.lang.System.out;

@CommandLine.Command(name = "add", description = "Add a password entry")
public class AddCommand extends BaseVaultCommand {

    @CommandLine.Option(names = {"--generate"}, description = "Generate a secure password")
    private boolean generatePassword;

    @CommandLine.Option(names = {"--password-length"}, description = "Generated password length", defaultValue = "16")
    private int passwordLength;

    @Override
    public Integer call() {
        try {
            VaultOperationResult result = authenticateAndLoadVault();
            if (result == null) {
                return 1;
            }

            Console console = getConsoleOrFail();
            if (console == null) {
                return 1;
            }

            String name = console.readLine("Entry name: ");
            
            // Validate entry name
            if (!SecurityUtils.isValidEntryName(name)) {
                err.println("Invalid entry name. Use alphanumeric characters, spaces, hyphens, underscores, and dots only.");
                return 1;
            }

            if (result.getVault().hasEntry(name)) {
                err.println("Entry with name '" + name + "' already exists.");
                return 1;
            }

            String username = console.readLine("Username: ");
            
            String password;
            if (generatePassword) {
                password = SecurityUtils.generateStrongPassword();
                out.println("Generated password: " + password);
                out.println("Strength: " + SecurityUtils.getPasswordStrengthDescription(password.toCharArray()));
            } else {
                char[] entryPassword = console.readPassword("Password: ");
                password = new String(entryPassword);
                
                // Check password strength
                if (!SecurityUtils.validatePasswordStrength(entryPassword)) {
                    String strength = SecurityUtils.getPasswordStrengthDescription(entryPassword);
                    out.println("Password strength: " + strength);
                    
                    String confirm = console.readLine("Continue with weak password? (y/N): ");
                    if (!confirm.equalsIgnoreCase("y")) {
                        SecurityUtils.secureClear(entryPassword);
                        return 1;
                    }
                }
                SecurityUtils.secureClear(entryPassword);
            }

            // Add entry
            result.getVault().addEntry(name, username, password);
            VaultFile.saveVault(result.getVault(), VaultPaths.PANDORA_FILE, result.getKey(), result.getSalt());
            
            VaultLogger.logDataModification("ADD_ENTRY", name);
            out.println(VaultConfig.getColoredMessage("Entry added: " + name, "success"));

            // Create auto-backup if enabled
            if (VaultConfig.isAutoBackupEnabled()) {
                try {
                    VaultBackup.createBackup(result.getKey(), result.getSalt());
                    VaultLogger.info("Auto-backup created after adding entry: " + name);
                } catch (Exception e) {
                    VaultLogger.warn("Failed to create auto-backup: " + e.getMessage());
                }
            }

            return 0;
        } catch (PandoraException e) {
            VaultLogger.error("Failed to add entry", e);
            err.println("Error: " + e.getMessage());
            return 1;
        } catch (Exception e) {
            VaultLogger.error("Unexpected error in AddCommand", e);
            err.println("Unexpected error: " + e.getMessage());
            return 1;
        }
    }
}
