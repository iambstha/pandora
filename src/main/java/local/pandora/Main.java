package local.pandora;

import local.pandora.command.*;
import local.pandora.config.VaultConfig;
import local.pandora.logging.VaultLogger;
import picocli.CommandLine;

import java.util.concurrent.Callable;

import static java.lang.System.*;

@CommandLine.Command(
        name = "pandora",
        mixinStandardHelpOptions = true,
        version = "Pandora 1.0.0",
        description = "Personal encrypted vault - Production Ready",
        headerHeading = "Pandora - Secure Password Manager%n%n",
        descriptionHeading = "%nDescription:%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        commandListHeading = "%nCommands:%n",
        footerHeading = "%nExamples:%n",
        footer = {
                "  pandora init                    Initialize a new vault",
                "  pandora add                     Add a new password entry",
                "  pandora get <name>              Retrieve password entry",
                "  pandora get <name> --copy       Copy password entry to the clipboard",
                "  pandora list                    List all entries",
                "  pandora update <name>           Update existing entry",
                "  pandora delete <name>           Delete specific entry",
                "  pandora delete --all            Delete all entries",
                "  pandora backup create           Create backup",
                "  pandora backup list             List backups",
                "  pandora backup restore <file>   Restore from backup",
                "  pandora security generate       Generate secure password",
                "  pandora security check <pass>  Check password strength",
                "  pandora upload                  Upload to cloud storage",
                "  pandora download <file-id>      Download from cloud storage"
        }
)
public class Main implements Callable<Integer> {

    @CommandLine.Option(names = {"-v", "--verbose"}, description = "Enable verbose logging")
    private boolean verbose;

    @CommandLine.Option(names = {"--config"}, description = "Path to configuration file")
    private String configPath;

    @CommandLine.Option(names = {"--no-color"}, description = "Disable colored output")
    private boolean noColor;

    public static void main(String[] args) {
        try {
            // Initialize configuration
            VaultConfig.initialize();
            
            // Setup logging
            VaultLogger.initialize();
            
            Main main = new Main();
            CommandLine cmd = new CommandLine(main);
            
            // Register commands
            cmd.addSubcommand(new InitCommand())
               .addSubcommand(new AddCommand())
               .addSubcommand(new DeleteCommand())
               .addSubcommand(new UpdateCommand())
               .addSubcommand(new GetCommand())
               .addSubcommand(new ListCommand())
               .addSubcommand(new BackupCommand())
               .addSubcommand(new SecurityCommand())
               .addSubcommand("upload", new UploadCommand())
               .addSubcommand("download", new DownloadCommand());
            
            // Execute command
            int exitCode = cmd.execute(args);
            
            // Cleanup resources
            VaultLogger.shutdown();
            
            exit(exitCode);
        } catch (Exception e) {
            err.println("Fatal error: " + e.getMessage());
            if (args.length > 0 && (args[0].equals("-v") || args[0].equals("--verbose"))) {
                e.printStackTrace();
            }
            exit(1);
        }
    }

    @Override
    public Integer call() {
        try {
            // Apply configuration
            if (verbose) {
                VaultLogger.setVerbose(true);
            }
            
            if (noColor) {
                VaultConfig.setColoredOutput(false);
            }
            
            if (configPath != null) {
                VaultConfig.loadConfig(configPath);
            }
            
            out.println(VaultConfig.getColoredMessage("Pandora v1.0.0 - Use --help to see available commands.", "info"));
            return 0;
        } catch (Exception e) {
            err.println("Error: " + e.getMessage());
            return 1;
        }
    }
}
