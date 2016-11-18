package eu.drus.test.persistence;

public class JpaTestException extends RuntimeException {

    private static final long serialVersionUID = 6564897685710323054L;

    /**
     * @param cause
     */
    public JpaTestException(final Throwable cause) {
        super(cause);
    }
}
