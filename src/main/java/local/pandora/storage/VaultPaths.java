package local.pandora.storage;

import java.nio.file.Path;

public class VaultPaths {

    public static final Path PANDORA_DIR = Path.of(System.getProperty("user.home"), ".pandora");

    public static final Path PANDORA_FILE = PANDORA_DIR.resolve("pandora.enc");

    public static final Path BACKUP_DIR = PANDORA_DIR.resolve("backups");

    public static final Path DRIVE_OAUTH_CREDENTIALS = PANDORA_DIR.resolve("credentials.json");
}
