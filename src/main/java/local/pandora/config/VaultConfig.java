package local.pandora.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import local.pandora.exception.PandoraException;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.*;

public class VaultConfig {

    private VaultConfig() {}
    
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final Path DEFAULT_CONFIG_PATH = Path.of(getProperty("user.home"), ".pandora", "config.yaml");
    
    private static VaultConfigInstance config = new VaultConfigInstance();

    @Getter
    @Setter
    private static boolean coloredOutput = true;
    
    public static void initialize() {
        try {
            if (Files.exists(DEFAULT_CONFIG_PATH)) {
                loadConfig(DEFAULT_CONFIG_PATH.toString());
            } else {
                createDefaultConfig();
            }
        } catch (Exception e) {
            err.println("Warning: Could not load configuration, using defaults: " + e.getMessage());
        }
    }
    
    public static void loadConfig(String configPath) {
        try {
            Path path = Path.of(configPath);
            if (!Files.exists(path)) {
                throw new PandoraException("Configuration file not found: " + configPath);
            }
            
            String content = Files.readString(path);
            if (configPath.endsWith(".yaml") || configPath.endsWith(".yml")) {
                config = yamlMapper.readValue(content, VaultConfigInstance.class);
            } else {
                config = jsonMapper.readValue(content, VaultConfigInstance.class);
            }
        } catch (IOException e) {
            throw new PandoraException("Failed to load configuration: " + e.getMessage(), e);
        }
    }
    
    public static void createDefaultConfig() {
        try {
            Files.createDirectories(DEFAULT_CONFIG_PATH.getParent());
            String defaultConfig = yamlMapper.writeValueAsString(config);
            Files.writeString(DEFAULT_CONFIG_PATH, defaultConfig);
        } catch (IOException e) {
            err.println("Warning: Could not create default configuration: " + e.getMessage());
        }
    }
    
    public static void saveConfig() {
        try {
            String configContent = yamlMapper.writeValueAsString(config);
            Files.writeString(DEFAULT_CONFIG_PATH, configContent);
        } catch (IOException e) {
            throw new PandoraException("Failed to save configuration: " + e.getMessage(), e);
        }
    }
    
    public static String getVaultDirectory() {
        return config.getPandoraDirectory();
    }
    
    public static String getBackupDirectory() {
        return config.getBackupDirectory();
    }
    
    public static int getBackupRetentionDays() {
        return config.getBackupRetentionDays();
    }
    
    public static boolean isAutoBackupEnabled() {
        return config.isAutoBackupEnabled();
    }
    
    public static String getCloudProvider() {
        return config.getCloudProvider();
    }
    
    public static Map<String, String> getCloudSettings() {
        return config.getCloudSettings();
    }

    public static String getColoredMessage(String message, String type) {
        if (!coloredOutput) {
            return message;
        }
        
        return switch (type.toLowerCase()) {
            case "success" -> "\u001B[32m" + message + "\u001B[0m"; // Green
            case "error" -> "\u001B[31m" + message + "\u001B[0m";   // Red
            case "warning" -> "\u001B[33m" + message + "\u001B[0m"; // Yellow
            case "info" -> "\u001B[36m" + message + "\u001B[0m";    // Cyan
            default -> message;
        };
    }
    
    @Setter
    @Getter
    public static class VaultConfigInstance {
        // Getters and setters
        private String pandoraDirectory = getProperty("user.home") + "/.pandora";
        private String backupDirectory = getProperty("user.home") + "/.pandora/backups";
        private int backupRetentionDays = 30;
        private boolean autoBackupEnabled = true;
        private String cloudProvider = "google-drive";
        private Map<String, String> cloudSettings = new HashMap<>();

    }
}
