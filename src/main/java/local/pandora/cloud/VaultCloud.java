package local.pandora.cloud;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import local.pandora.constant.PandoraConstant;
import local.pandora.exception.PandoraException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


import static java.lang.System.*;

public class VaultCloud {

    private VaultCloud() {}

    private static final String MIME_TYPE_OCTET_STREAM = "application/octet-stream";

    public static void uploadVault(Path vaultPath) throws PandoraException {
        validateUploadInputs(vaultPath);
        
        try {
            Drive drive = DriveService.getDriveService();
            
            File fileMetadata = new File();
            fileMetadata.setName(PandoraConstant.VAULT_FILE_NAME);

            java.io.File filePath = vaultPath.toFile();
            com.google.api.client.http.FileContent mediaContent =
                    new com.google.api.client.http.FileContent(MIME_TYPE_OCTET_STREAM, filePath);

            File uploadedFile = drive.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();

            out.println("Pandora uploaded to Google Drive. File ID: " + uploadedFile.getId());
        } catch (Exception e) {
            throw new PandoraException("Failed to upload Pandora to Google Drive", e);
        }
    }

    public static void downloadPandora(Path vaultPath, String fileId) throws PandoraException {
        validateDownloadInputs(vaultPath, fileId);
        
        try {
            Drive drive = DriveService.getDriveService();
            
            try (var outputStream = Files.newOutputStream(vaultPath)) {
                drive.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            }

            out.println("Pandora downloaded from Drive to " + vaultPath);
        } catch (Exception e) {
            throw new PandoraException("Failed to download Pandora from Google Drive", e);
        }
    }

    private static void validateUploadInputs(Path vaultPath) throws PandoraException {
        if (vaultPath == null) {
            throw new IllegalArgumentException("Pandora path cannot be null");
        }
        if (!Files.exists(vaultPath)) {
            throw new PandoraException("Pandora file does not exist: " + vaultPath);
        }
        if (!Files.isRegularFile(vaultPath)) {
            throw new PandoraException("Pandora path is not a regular file: " + vaultPath);
        }
        if (!Files.isReadable(vaultPath)) {
            throw new PandoraException("Pandora file is not readable: " + vaultPath);
        }
    }

    private static void validateDownloadInputs(Path vaultPath, String fileId) throws PandoraException {
        if (vaultPath == null) {
            throw new IllegalArgumentException("Pandora path cannot be null");
        }
        if (fileId == null || fileId.trim().isEmpty()) {
            throw new IllegalArgumentException("File ID cannot be null or empty");
        }
        
        try {
            Files.createDirectories(vaultPath.getParent());
        } catch (IOException e) {
            throw new PandoraException("Failed to create parent directories: " + vaultPath.getParent(), e);
        }
    }
}