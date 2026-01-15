package local.pandora.storage;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class VaultEntry {
    private String username;
    private String password;

    public VaultEntry() {}

    public VaultEntry(String username, String password) {
        this.username = username;
        this.password = password;
    }

}
