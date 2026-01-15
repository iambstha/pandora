package local.pandora.backup;

import local.pandora.config.VaultConfig;
import local.pandora.constant.PandoraConstant;
import local.pandora.exception.PandoraException;
import local.pandora.logging.VaultLogger;
import local.pandora.storage.VaultFile;
import local.pandora.storage.VaultPaths;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.System.out;

public class VaultBackup {
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    private VaultBackup() {}


    
    public static void createBackup(SecretKey key, byte[] salt) throws PandoraException {
        try {
            Path backupDir = Paths.get(VaultConfig.getBackupDirectory());
            Files.createDirectories(backupDir);
            
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String backupFileName = PandoraConstant.PANDORA_BACKUP_PREFIX + timestamp + ".enc";
            Path backupPath = backupDir.resolve(backupFileName);
            
            // Create backup by copying current vault file
            Path currentVault = VaultPaths.PANDORA_FILE;
            if (Files.exists(currentVault)) {
                Files.copy(currentVault, backupPath);
                VaultLogger.info("Created backup: " + backupFileName);
                out.println(VaultConfig.getColoredMessage("✓ Backup created: " + backupFileName, "success"));
            }
            
            // Clean up old backups
            cleanupOldBackups();
            
        } catch (IOException e) {
            throw new PandoraException("Failed to create backup", e);
        }
    }
    
    public static void restoreBackup(String backupFileName, SecretKey key, byte[] salt) throws PandoraException {
        try {
            Path backupDir = Paths.get(VaultConfig.getBackupDirectory());
            Path backupPath = backupDir.resolve(backupFileName);
            
            if (!Files.exists(backupPath)) {
                throw new PandoraException("Backup file not found: " + backupFileName);
            }
            
            // Verify backup integrity before restoring
            if (!verifyBackupIntegrity(backupPath)) {
                throw new PandoraException("Backup file is corrupted or invalid: " + backupFileName);
            }
            
            // Create backup of current vault before restoring
            if (Files.exists(VaultPaths.PANDORA_FILE)) {
                createBackup(key, salt);
            }
            
            // Restore from backup
            Files.copy(backupPath, VaultPaths.PANDORA_FILE, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            VaultLogger.info("Restored backup: " + backupFileName);
            out.println(VaultConfig.getColoredMessage("✓ Backup restored: " + backupFileName, "success"));
            
        } catch (IOException e) {
            throw new PandoraException("Failed to restore backup", e);
        }
    }
    
    public static List<String> listBackups() throws PandoraException {
        try {
            Path backupDir = Paths.get(VaultConfig.getBackupDirectory());
            if (!Files.exists(backupDir)) {
                return List.of();
            }
            
            try (Stream<Path> stream = Files.list(backupDir)) {
                return stream
                        .filter(path -> path.getFileName().toString().startsWith(PandoraConstant.PANDORA_BACKUP_PREFIX) &&
                                       path.getFileName().toString().endsWith(".enc"))
                        .map(path -> path.getFileName().toString())
                        .sorted(Comparator.reverseOrder())
                        .toList();
            }
        } catch (IOException e) {
            throw new PandoraException("Failed to list backups", e);
        }
    }
    
    public static void deleteBackup(String backupFileName) throws PandoraException {
        try {
            Path backupDir = Paths.get(VaultConfig.getBackupDirectory());
            Path backupPath = backupDir.resolve(backupFileName);
            
            if (!Files.exists(backupPath)) {
                throw new PandoraException("Backup file not found: " + backupFileName);
            }
            
            Files.delete(backupPath);
            VaultLogger.info("Deleted backup: " + backupFileName);
            out.println(VaultConfig.getColoredMessage("✓ Backup deleted: " + backupFileName, "success"));
            
        } catch (IOException e) {
            throw new PandoraException("Failed to delete backup", e);
        }
    }
    
    private static void cleanupOldBackups() throws IOException {
        int retentionDays = VaultConfig.getBackupRetentionDays();
        if (retentionDays <= 0) {
            return; // No cleanup if retention is disabled
        }
        
        Path backupDir = Paths.get(VaultConfig.getBackupDirectory());
        if (!Files.exists(backupDir)) {
            return;
        }
        
        long cutoffTime = System.currentTimeMillis() - (long) retentionDays * 24 * 60 * 60 * 1000;
        
        try (Stream<Path> stream = Files.list(backupDir)) {
            stream.filter(path -> path.getFileName().toString().startsWith(PandoraConstant.PANDORA_BACKUP_PREFIX) &&
                              path.getFileName().toString().endsWith(".enc"))
                  .filter(path -> {
                      try {
                          return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                      } catch (IOException e) {
                          return false;
                      }
                  })
                  .forEach(path -> {
                      try {
                          Files.delete(path);
                          VaultLogger.debug("Deleted old backup: " + path.getFileName());
                      } catch (IOException e) {
                          VaultLogger.warn("Failed to delete old backup: " + path.getFileName());
                      }
                  });
        }
    }
    
    private static boolean verifyBackupIntegrity(Path backupPath) {
        try {
            // Basic integrity check - ensure file is not empty and has expected structure
            if (Files.size(backupPath) == 0) {
                return false;
            }
            
            // Try to load the vault container to verify it's valid
            VaultFile.loadVaultContainer(backupPath);
            return true;
            
        } catch (Exception e) {
            VaultLogger.warn("Backup integrity check failed for: " + backupPath.getFileName());
            return false;
        }
    }
    
    public static void enableAutoBackup() {
        if (VaultConfig.isAutoBackupEnabled()) {
            VaultLogger.info("Auto backup is enabled");
        } else {
            VaultLogger.info("Auto backup is disabled");
        }
    }
}
