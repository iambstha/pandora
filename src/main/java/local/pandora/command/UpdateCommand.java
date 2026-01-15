package local.pandora.command;

import local.pandora.exception.PandoraException;
import local.pandora.storage.VaultPaths;
import local.pandora.storage.VaultFile;
import picocli.CommandLine;

import java.io.Console;

import static java.lang.System.*;

@CommandLine.Command(name = "update", description = "Update an existing password entry")
public class UpdateCommand extends BaseVaultCommand {

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

            String entryName = console.readLine("Entry name to update: ");
            var entry = result.getVault().getEntry(entryName);
            if (entry == null) {
                err.println("No such entry: " + entryName);
                return 1;
            }

            String newUsername = console.readLine("New username (leave blank to keep current): ");
            char[] newPasswordChars = console.readPassword("New password (leave blank to keep current): ");
            String newPassword = newPasswordChars.length > 0 ? new String(newPasswordChars) : entry.getPassword();

            if (!newUsername.isBlank()) entry.setUsername(newUsername);
            entry.setPassword(newPassword);

            VaultFile.saveVault(result.getVault(), VaultPaths.PANDORA_FILE, result.getKey(), result.getSalt());
            out.println("Successfully updated entry: " + entryName);

            return 0;
        } catch (PandoraException e) {
            err.println("Error: " + e.getMessage());
            return 1;
        } catch (Exception e) {
            err.println("Unexpected error: " + e.getMessage());
            return 1;
        }
    }
}
