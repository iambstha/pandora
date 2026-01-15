package local.pandora.command;

import local.pandora.backup.VaultBackup;
import local.pandora.exception.PandoraException;
import picocli.CommandLine;

import static java.lang.System.err;
import static java.lang.System.out;

@CommandLine.Command(
    name = "backup", 
    description = "Manage vault backups",
    subcommands = {
        BackupCommand.Create.class,
        BackupCommand.Restore.class,
        BackupCommand.List.class,
        BackupCommand.Delete.class
    }
)
public class BackupCommand extends BaseVaultCommand {
    
    @Override
    public Integer call() {
        out.println("Use 'vault backup --help' to see available backup commands.");
        return 0;
    }
    
    @CommandLine.Command(name = "create", description = "Create a backup of the vault")
    public static class Create extends BaseVaultCommand {
        
        @Override
        public Integer call() {
            try {
                if (!validateVaultExists()) {
                    return 1;
                }
                
                VaultOperationResult result = authenticateAndLoadVault();
                if (result == null) {
                    return 1;
                }
                
                VaultBackup.createBackup(result.getKey(), result.getSalt());
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
    
    @CommandLine.Command(name = "restore", description = "Restore a vault from backup")
    public static class Restore extends BaseVaultCommand {
        
        @CommandLine.Parameters(description = "Backup file name to restore")
        private String backupFileName;
        
        @Override
        public Integer call() {
            try {
                if (!validateVaultExists()) {
                    return 1;
                }
                
                VaultOperationResult result = authenticateAndLoadVault();
                if (result == null) {
                    return 1;
                }
                
                VaultBackup.restoreBackup(backupFileName, result.getKey(), result.getSalt());
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
    
    @CommandLine.Command(name = "list", description = "List all available backups")
    public static class List extends BaseVaultCommand {
        
        @Override
        public Integer call() {
            try {
                java.util.List<String> backups = VaultBackup.listBackups();
                
                if (backups.isEmpty()) {
                    out.println("No backups found.");
                    return 0;
                }
                
                out.println("Available backups:");
                backups.forEach(backup -> out.println("  " + backup));
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
    
    @CommandLine.Command(name = "delete", description = "Delete a backup")
    public static class Delete extends BaseVaultCommand {
        
        @CommandLine.Parameters(description = "Backup file name to delete")
        private String backupFileName;
        
        @Override
        public Integer call() {
            try {
                VaultBackup.deleteBackup(backupFileName);
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
}
