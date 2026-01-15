package local.pandora.cloud;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import local.pandora.constant.PandoraConstant;
import local.pandora.exception.PandoraException;
import local.pandora.storage.VaultPaths;

import java.io.FileReader;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import static java.lang.System.*;

public class DriveService {

    private DriveService() {}

    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final int LOCAL_SERVER_PORT = 8888;
    private static Drive drive;

    private static final List<String> DRIVE_SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);

    public static synchronized Drive getDriveService() throws PandoraException {
        if (drive != null) {
            return drive;
        }

        try {
            var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            
            validateCredentialsFile();
            
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                    new FileReader(VaultPaths.DRIVE_OAUTH_CREDENTIALS.toString()));

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, clientSecrets, DRIVE_SCOPES)
                    .setAccessType("offline")
                    .build();

            var credential = new AuthorizationCodeInstalledApp(
                    flow, 
                    new LocalServerReceiver.Builder().setPort(LOCAL_SERVER_PORT).build()
            ).authorize("user");

            drive = new Drive.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(PandoraConstant.APPLICATION_NAME)
                    .build();

            return drive;
        } catch (Exception e) {
            throw new PandoraException("Failed to initialize Google Drive service", e);
        }
    }

    private static void validateCredentialsFile() throws PandoraException {
        if (!Files.exists(VaultPaths.DRIVE_OAUTH_CREDENTIALS)) {
            // Try to copy credentials.json from classpath to user directory
            copyCredentialsFromClasspath();
        }
        
        if (!Files.exists(VaultPaths.DRIVE_OAUTH_CREDENTIALS)) {
            throw new PandoraException("Google Drive credentials file not found: " + VaultPaths.DRIVE_OAUTH_CREDENTIALS);
        }
        
        if (!Files.isRegularFile(VaultPaths.DRIVE_OAUTH_CREDENTIALS)) {
            throw new PandoraException("Credentials path is not a regular file: " + VaultPaths.DRIVE_OAUTH_CREDENTIALS);
        }
        
        if (!Files.isReadable(VaultPaths.DRIVE_OAUTH_CREDENTIALS)) {
            throw new PandoraException("Credentials file is not readable: " + VaultPaths.DRIVE_OAUTH_CREDENTIALS);
        }
    }
    
    private static void copyCredentialsFromClasspath() throws PandoraException {
        try {
            // Try to get credentials.json from classpath
            var inputStream = DriveService.class.getResourceAsStream("/credentials.json");
            if (inputStream != null) {
                // Ensure parent directory exists
                Files.createDirectories(VaultPaths.DRIVE_OAUTH_CREDENTIALS.getParent());
                
                // Copy to user directory
                Files.copy(inputStream, VaultPaths.DRIVE_OAUTH_CREDENTIALS);
                out.println("Credentials file copied to: " + VaultPaths.DRIVE_OAUTH_CREDENTIALS);
                return;
            }
        } catch (Exception e) {
            // If copy fails, continue and let the user know manually
        }
        
        err.println("Warning: credentials.json not found in classpath or failed to copy.");
        err.println("Please copy credentials.json to: " + VaultPaths.DRIVE_OAUTH_CREDENTIALS);
    }
}
