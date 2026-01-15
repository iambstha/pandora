package local.pandora.command;

import local.pandora.cloud.VaultCloud;
import local.pandora.exception.PandoraException;
import local.pandora.storage.VaultPaths;
import picocli.CommandLine;

import static java.lang.System.*;

@CommandLine.Command(name = "download", description = "Download pandora.enc from Google Drive")
public class DownloadCommand extends BaseVaultCommand {

    @CommandLine.Parameters(index = "0", description = "Google Drive file ID of pandora.enc")
    private String fileId;

    @Override
    public Integer call() {
        try {
            if (!validateVaultExists()) {
                return 1;
            }

            VaultCloud.downloadPandora(VaultPaths.PANDORA_FILE, fileId);
            out.println("Pandora downloaded from Google Drive Successfully.");
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
