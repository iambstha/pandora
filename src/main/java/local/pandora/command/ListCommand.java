package local.pandora.command;

import picocli.CommandLine;

import static java.lang.System.*;

@CommandLine.Command(name = "list", description = "List all entry names")
public class ListCommand extends BaseVaultCommand {

    @Override
    public Integer call() {
        try {
            VaultOperationResult result = authenticateAndLoadVault();
            if (result == null) {
                return 1;
            }

            result.getVault().getAllEntries().keySet().forEach(out::println);
            return 0;
        } catch (Exception e) {
            err.println("Unexpected error: " + e.getMessage());
            return 1;
        }
    }
}
