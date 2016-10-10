package eu.drus.test.persistence;

public class JpaTestException extends RuntimeException {

    private static final long serialVersionUID = 6564897685710323054L;

    /**
     * @param message
     */
    public JpaTestException(final String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public JpaTestException(final Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public JpaTestException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
