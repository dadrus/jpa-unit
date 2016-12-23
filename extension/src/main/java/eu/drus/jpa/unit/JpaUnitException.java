package eu.drus.jpa.unit;

public class JpaUnitException extends RuntimeException {

    private static final long serialVersionUID = 6564897685710323054L;

    public JpaUnitException(final String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public JpaUnitException(final Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public JpaUnitException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
