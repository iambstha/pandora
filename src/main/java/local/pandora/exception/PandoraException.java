package local.pandora.exception;

public class PandoraException extends RuntimeException {

    public PandoraException() {
        super("Something went wrong");
    }

    public PandoraException(String message, Exception e) {
        super(message, e);
    }

    public PandoraException(Throwable cause) {
        super("Something went wrong", cause);
    }

    public PandoraException(String message) {
        super(message);
    }
}
