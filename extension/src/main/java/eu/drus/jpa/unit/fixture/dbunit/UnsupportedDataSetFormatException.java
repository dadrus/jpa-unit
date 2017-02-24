package eu.drus.jpa.unit.fixture.dbunit;

public class UnsupportedDataSetFormatException extends RuntimeException {

    private static final long serialVersionUID = 5725714637732803589L;

    public UnsupportedDataSetFormatException(final String message) {
        super(message);
    }
}
