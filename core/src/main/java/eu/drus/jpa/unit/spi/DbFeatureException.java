package eu.drus.jpa.unit.spi;

public class DbFeatureException extends Exception {

    private static final long serialVersionUID = 446169485018771790L;

    public DbFeatureException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
