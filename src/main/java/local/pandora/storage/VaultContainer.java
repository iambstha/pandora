package local.pandora.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class VaultContainer {
    private String salt; // Base64-encoded
    private String encryptedData; // AES/GCM-encrypted JSON of Vault
}
