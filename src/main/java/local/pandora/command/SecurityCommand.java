package local.pandora.command;

import local.pandora.security.SecurityUtils;
import picocli.CommandLine;

import static java.lang.System.err;
import static java.lang.System.out;

@CommandLine.Command(
    name = "security", 
    description = "Security utilities",
    subcommands = {
        SecurityCommand.GeneratePassword.class,
        SecurityCommand.CheckPassword.class,
        SecurityCommand.Status.class
    }
)
public class SecurityCommand extends BaseVaultCommand {
    
    @Override
    public Integer call() {
        out.println("Use 'vault security --help' to see available security commands.");
        return 0;
    }
    
    @CommandLine.Command(name = "generate", description = "Generate a secure password")
    public static class GeneratePassword extends BaseVaultCommand {
        
        @CommandLine.Option(names = {"-l", "--length"}, description = "Password length", defaultValue = "16")
        private int length;
        
        @CommandLine.Option(names = {"--no-uppercase"}, description = "Exclude uppercase letters")
        private boolean noUppercase;
        
        @CommandLine.Option(names = {"--no-lowercase"}, description = "Exclude lowercase letters")
        private boolean noLowercase;
        
        @CommandLine.Option(names = {"--no-numbers"}, description = "Exclude numbers")
        private boolean noNumbers;
        
        @CommandLine.Option(names = {"--no-special"}, description = "Exclude special characters")
        private boolean noSpecial;
        
        @Override
        public Integer call() {
            try {
                String password = SecurityUtils.generatePassword(
                    length, 
                    !noUppercase, 
                    !noLowercase, 
                    !noNumbers, 
                    !noSpecial
                );
                
                out.println("Generated password: " + password);
                out.println("Strength: " + SecurityUtils.getPasswordStrengthDescription(password.toCharArray()));
                
                return 0;
            } catch (Exception e) {
                err.println("Error: " + e.getMessage());
                return 1;
            }
        }
    }
    
    @CommandLine.Command(name = "check", description = "Check password strength")
    public static class CheckPassword extends BaseVaultCommand {
        
        @CommandLine.Parameters(description = "Password to check")
        private String password;
        
        @Override
        public Integer call() {
            try {
                SecurityUtils.PasswordValidator validator = new SecurityUtils.PasswordValidator(password);
                
                if (validator.isValid()) {
                    out.println("✓ Password meets all security requirements");
                } else {
                    out.println("✗ Password does not meet security requirements:");
                    out.println("  " + validator.getValidationMessage());
                }
                
                out.println("Strength: " + SecurityUtils.getPasswordStrengthDescription(password.toCharArray()));
                
                return 0;
            } catch (Exception e) {
                err.println("Error: " + e.getMessage());
                return 1;
            }
        }
    }
    
    @CommandLine.Command(name = "status", description = "Show security status")
    public static class Status extends BaseVaultCommand {
        
        @Override
        public Integer call() {
            try {
                out.println("Security Status:");
                out.println("  Account Locked: " + (SecurityUtils.isLockedOut() ? "Yes" : "No"));
                
                if (SecurityUtils.isLockedOut()) {
                    long remaining = SecurityUtils.getRemainingLockoutTime();
                    out.println("  Lockout Remaining: " + (remaining / 1000 / 60) + " minutes");
                }
                
                SecurityUtils.checkMemoryLeak();
                
                return 0;
            } catch (Exception e) {
                err.println("Error: " + e.getMessage());
                return 1;
            }
        }
    }
}
