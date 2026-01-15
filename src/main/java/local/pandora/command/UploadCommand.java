package local.pandora.command;

import local.pandora.cloud.VaultCloud;
import local.pandora.exception.PandoraException;
import local.pandora.storage.VaultPaths;
import picocli.CommandLine;

import static java.lang.System.*;

@CommandLine.Command(name = "upload", description = "Upload pandora.enc to Google Drive")
public class UploadCommand extends BaseVaultCommand {

    @Override
    public Integer call() {
        try {
            if (!validateVaultExists()) {
                return 1;
            }

            VaultCloud.uploadVault(VaultPaths.PANDORA_FILE);
            out.println("Pandora uploaded to Google Drive Successfully");
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
