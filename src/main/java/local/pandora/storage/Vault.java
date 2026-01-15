package local.pandora.storage;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import local.pandora.exception.PandoraException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class Vault {
    private final Map<String, VaultEntry> entries = new HashMap<>();

    public void addEntry(String name, String username, String password) {
        validateEntryName(name);
        validateCredentials(username, password);
        entries.put(name, new VaultEntry(username, password));
    }

    public VaultEntry getEntry(String name) {
        return entries.get(name);
    }

    public void removeEntry(String name) {
        entries.remove(name);
    }

    public boolean hasEntry(String name) {
        return entries.containsKey(name);
    }

    public Set<String> getEntryNames() {
        return new HashSet<>(entries.keySet());
    }

    public Map<String, VaultEntry> getAllEntries() {
        return new HashMap<>(entries);
    }
    
    public void setAllEntries(Map<String, VaultEntry> entries) {
        this.entries.clear();
        this.entries.putAll(entries);
    }

    public void clearEntries() {
        entries.clear();
    }

    public int getEntryCount() {
        return entries.size();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    private void validateEntryName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new PandoraException("Entry name cannot be null or empty");
        }
        if (entries.containsKey(name)) {
            throw new PandoraException("Entry with name '" + name + "' already exists");
        }
    }

    private void validateCredentials(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new PandoraException("Username cannot be null or empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new PandoraException("Password cannot be null or empty");
        }
    }
}
