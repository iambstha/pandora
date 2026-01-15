# 🔐 Pandora - Secure Password Manager

A production-ready, secure password manager built with Java and Maven.

## ✨ Features

### 🔐 Security
- **AES-GCM Encryption**: Military-grade encryption with 256-bit keys
- **PBKDF2 Key Derivation**: Industry-standard key stretching with salt
- **Secure Password Input**: Passwords entered via console (no echo)
- **Password Strength Validation**: Built-in password strength checker
- **Memory Security**: Secure password clearing from memory

### 🌩 Cloud Integration
- **Google Drive Backup**: Automatic and manual backups to Google Drive
- **OAuth2 Authentication**: Secure Google Drive integration
- **File Synchronization**: Upload/download encrypted vault files

### 🛠️ Management
- **Add/Update/Delete**: Full CRUD operations for password entries
- **Entry Search**: Quick lookup of stored credentials
- **Batch Operations**: Delete all entries at once
- **Auto-backup**: Automatic backups after modifications

### 📊 Advanced Features
- **Configuration Management**: YAML-based configuration
- **Comprehensive Logging**: Detailed operation logging
- **Error Handling**: Robust exception management
- **Cross-platform**: Works on Linux, macOS, Windows

## 🚀 Quick Start

### Prerequisites
- Java 21 or higher
- Maven 3.6 or higher
- Google Drive account (for cloud features)

### Installation
```bash
# Clone the repository
git clone <repository-url>
cd pandora

# Build the application
mvn clean compile package

# The executable JAR will be created at:
# target/pandora-1.0-SNAPSHOT.jar
```

### First Time Setup

Before executing commands below make sure to edit the `pandora` file that is in the project root directory and copy it to your `/usr/local/bin/` directory and make it executable with `chmod +x pandora`

### Basic Commands
```bash
# Initialize vault
pandora init

# Add new entry
pandora add

# List all entries
pandora list

# Get specific entry
pandora get <entry-name>

# Update existing entry
pandora update <entry-name>

# Delete entry
pandora delete <entry-name>

# Delete all entries
pandora delete --all
```

### Cloud Operations
```bash
# Upload to Google Drive
pandora upload

# Download from Google Drive
pandora download <file-id>

# Manage backups
pandora backup list
pandora backup create
pandora backup restore <backup-file>
```

### Security Operations
```bash
# Check password strength
pandora security check <password>

# Generate strong password
pandora security generate

# Security audit
pandora security audit
```

## ⚙️ Configuration

Edit `~/.pandora/config.yaml` to customize behavior:

```yaml
# Pandora Configuration File
# This file controls behavior of Pandora

# Pandora Settings
pandora_directory: "${user.home}/.pandora"
backup_directory: "${user.home}/.pandora/backups"

# Backup Settings
backup_retention_days: 30
auto_backup_enabled: true

# Cloud Storage Settings
cloud_provider: "google-drive"
cloud_settings:
  default_folder: "Pandora Backups"
  encryption_enabled: true

# Security Settings
password_min_length: 12
require_special_chars: true
require_numbers: true
require_uppercase: true

# Logging Settings
log_level: "INFO"
log_file: "${user.home}/.pandora/logs/pandora.log"
```

### Build Commands
```bash
# Clean build
mvn clean

# Compile only
mvn compile

# Run tests
mvn test

# Package application
mvn package

# Skip tests during packaging
mvn package -DskipTests

# Create shaded JAR with all dependencies
mvn clean package
```

### Dependencies
- **Jackson 2.20.1**: JSON serialization/deserialization
- **BouncyCastle 1.78**: Cryptography provider
- **Picocli 4.7.6**: Command-line interface
- **Google APIs**: Drive integration
- **SnakeYAML 2.4**: Configuration file support

## 🔒 Security Details

### Encryption Process
1. **Key Generation**: PBKDF2 with HMAC-SHA256, 100,000 iterations
2. **Encryption**: AES-256-GCM with 96-bit authentication tag
3. **IV Generation**: Cryptographically secure random IV per encryption
4. **Salt Storage**: Base64 encoded salt stored with encrypted data

### Security Best Practices
- ✅ No passwords in memory longer than necessary
- ✅ Secure random number generation
- ✅ Constant-time comparison for sensitive data
- ✅ Memory clearing after use
- ✅ Input validation and sanitization
- ✅ Comprehensive error handling

## 🌐 Cloud Integration

### Google Drive Setup
1. **Enable Google Drive API**
   - Go to Google Cloud Console
   - Enable Drive API
   - Create OAuth 2.0 credentials
   - Download `credentials.json`

2. **Configure Application**
   - Place `credentials.json` in project root or `~/.pandora/`
   - Application will auto-copy credentials during first run

3. **Backup Operations**
   - Automatic backups after each modification
   - Manual upload/download operations
   - Encrypted backup files with timestamps

## 📝 Logging

Pandora provides comprehensive logging for debugging and auditing:

```bash
# Log location
~/.pandora/logs/pandora.log

# Log levels
- ERROR: Critical errors
- WARN:  Warning messages
- INFO:  General information
- DEBUG: Detailed debugging
```

## 🐛 Troubleshooting

### Common Issues

**"Google Drive credentials file not found"**
- Ensure `credentials.json` exists in `~/.pandora/`
- Or place it in project root (will be auto-copied)
- Verify Google Drive API is enabled

**"Failed to decrypt vault file"**
- Verify master password is correct
- Check if vault file is corrupted
- Ensure vault file permissions are correct

**"Permission denied"**
- Check file permissions on `~/.pandora/`
- Ensure Java has read/write access
- Verify directory ownership

### Debug Mode
Enable debug logging in `config.yaml`:
```yaml
logging:
  log_level: "DEBUG"
```

## 📄 License

This project is licensed under the MIT License - see LICENSE file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## 📞 Support

For issues, questions, or contributions:
- Create an issue in the repository
- Check existing issues for solutions
- Review documentation before asking questions

---

**🔐 Pandora - Your Secure Digital Vault**
