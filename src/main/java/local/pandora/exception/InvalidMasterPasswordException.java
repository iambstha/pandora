package local.pandora.exception;

public class InvalidMasterPasswordException extends RuntimeException {

    public InvalidMasterPasswordException() {
        super("Invalid master password or corrupted vault");
    }

    public InvalidMasterPasswordException(Throwable cause) {
        super("Invalid master password or corrupted vault", cause);
    }
}
