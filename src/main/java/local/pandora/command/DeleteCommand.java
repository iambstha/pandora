package local.pandora.command;

import local.pandora.exception.PandoraException;
import local.pandora.storage.VaultPaths;
import local.pandora.storage.VaultFile;
import local.pandora.util.Generator;
import picocli.CommandLine;

import java.io.Console;

import static java.lang.System.*;

@CommandLine.Command(name = "delete", description = "Delete a password entry or wipe entire vault")
public class DeleteCommand extends BaseVaultCommand {

    @CommandLine.Parameters(index = "0", description = "Name of the entry to delete", arity = "0..1")
    private String entryName;

    @CommandLine.Option(names = "--all", description = "Delete all entries (wipe vault)")
    private boolean deleteAll;

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

            if (deleteAll) {
                String confirmation = Generator.generateRandomString(5);
                out.println("⚠️  You are about to delete ALL entries!");
                out.println("Type the following to confirm: " + confirmation);

                String input = new String(console.readPassword("> "));
                if (!confirmation.equals(input)) {
                    err.println("Confirmation did not match. Aborting.");
                    return 1;
                }

                result.getVault().clearEntries();
                VaultFile.saveVault(result.getVault(), VaultPaths.PANDORA_FILE, result.getKey(), result.getSalt());
                out.println("All entries deleted successfully.");
                return 0;
            }

            if (entryName == null) {
                err.println("Specify an entry name or use --all to delete everything.");
                return 1;
            }

            if (result.getVault().getEntry(entryName) == null) {
                err.println("No such entry: " + entryName);
                return 1;
            }

            result.getVault().removeEntry(entryName);
            VaultFile.saveVault(result.getVault(), VaultPaths.PANDORA_FILE, result.getKey(), result.getSalt());
            out.println("Successfully deleted entry: " + entryName);

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
