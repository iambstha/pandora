package local.pandora.command;

import local.pandora.exception.PandoraException;
import picocli.CommandLine;

import static java.lang.System.*;

@CommandLine.Command(name = "get", description = "Retrieve username/password for an entry")
public class GetCommand extends BaseVaultCommand {

    @CommandLine.Parameters(index = "0", description = "Entry name to retrieve")
    private String entryName;

    @CommandLine.Option(names = {"--copy"}, description = "Copy password to clipboard instead of printing")
    private boolean copy;

    @Override
    public Integer call() {
        try {
            VaultOperationResult result = authenticateAndLoadVault();
            if (result == null) {
                return 1;
            }

            var entry = result.getVault().getEntry(entryName);
            if (entry == null) {
                err.println("No such entry: " + entryName);
                return 1;
            }

            if (copy) {
                copyToClipboard(entry.getPassword());
                out.println("Password copied to clipboard");
            } else {
                out.println("Username: " + entry.getUsername());
                out.println("Password: " + entry.getPassword());
            }

            return 0;
        } catch (PandoraException e) {
            err.println("Error: " + e.getMessage());
            return 1;
        } catch (Exception e) {
            err.println("Unexpected error: " + e.getMessage());
            return 1;
        }
    }

    public static void copyToClipboard(String text) throws Exception {
        String os = getProperty("os.name").toLowerCase();
        Process p;
        if (os.contains("linux")) {
            p = new ProcessBuilder("xclip", "-selection", "clipboard").start();
        } else if (os.contains("mac")) {
            p = new ProcessBuilder("pbcopy").start();
        } else if (os.contains("windows")) {
            p = new ProcessBuilder("cmd", "/c", "clip").start();
        } else {
            throw new PandoraException("Clipboard not supported on " + os);
        }
        try (var osStream = p.getOutputStream()) {
            osStream.write(text.getBytes());
            osStream.flush();
        }
        p.waitFor();
    }
}
