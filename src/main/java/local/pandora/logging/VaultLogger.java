package local.pandora.logging;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.lang.System.*;

public class VaultLogger {
    
    private static final Path LOG_DIR = Paths.get(System.getProperty("user.home"), ".vaultcli", "logs");
    private static final Path LOG_FILE = LOG_DIR.resolve("vault.log");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private static boolean verbose = false;
    private static boolean initialized = false;
    private static PrintWriter logWriter;
    
    public static void initialize() {
        try {
            Files.createDirectories(LOG_DIR);
            logWriter = new PrintWriter(new FileWriter(LOG_FILE.toFile(), true), true);
            initialized = true;
            info("VaultLogger initialized");
        } catch (IOException e) {
            err.println("Warning: Could not initialize logging: " + e.getMessage());
        }
    }
    
    public static void shutdown() {
        if (logWriter != null) {
            info("VaultLogger shutting down");
            logWriter.close();
        }
    }
    
    public static void setVerbose(boolean verbose) {
        VaultLogger.verbose = verbose;
    }
    
    public static void info(String message) {
        log("INFO", message);
        if (verbose) {
            out.println("[INFO] " + message);
        }
    }
    
    public static void warn(String message) {
        log("WARN", message);
        if (verbose) {
            out.println("[WARN] " + message);
        }
    }
    
    public static void error(String message) {
        log("ERROR", message);
        err.println("[ERROR] " + message);
    }
    
    public static void error(String message, Throwable throwable) {
        log("ERROR", message + " - " + throwable.getMessage());
        err.println("[ERROR] " + message);
        if (verbose) {
            throwable.printStackTrace();
        }
    }
    
    public static void debug(String message) {
        if (verbose) {
            log("DEBUG", message);
            out.println("[DEBUG] " + message);
        }
    }
    
    public static void security(String message) {
        log("SECURITY", message);
        if (verbose) {
            out.println("[SECURITY] " + message);
        }
    }
    
    public static void audit(String action, String details) {
        String message = String.format("AUDIT: %s - %s", action, details);
        log("AUDIT", message);
        if (verbose) {
            out.println("[AUDIT] " + message);
        }
    }
    
    private static void log(String level, String message) {
        if (!initialized || logWriter == null) {
            return;
        }
        
        try {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String logEntry = String.format("[%s] %s: %s", timestamp, level, message);
            logWriter.println(logEntry);
        } catch (Exception e) {
            // Avoid infinite recursion if logging fails
            err.println("Failed to write to log file: " + e.getMessage());
        }
    }
    
    public static void logCommand(String command, String[] args) {
        audit("COMMAND_EXECUTION", command + " " + String.join(" ", args));
    }
    
    public static void logVaultAccess(String operation) {
        security("VAULT_ACCESS: " + operation);
    }
    
    public static void logAuthenticationAttempt(boolean success) {
        security("AUTHENTICATION_ATTEMPT: " + (success ? "SUCCESS" : "FAILURE"));
    }
    
    public static void logDataModification(String operation, String target) {
        audit("DATA_MODIFICATION", operation + " on " + target);
    }
    
    public static void logCloudOperation(String operation, String provider) {
        audit("CLOUD_OPERATION", operation + " with " + provider);
    }
}
