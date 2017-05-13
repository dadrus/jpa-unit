package eu.drus.jpa.unit.core;

public class UnsupportedDataSetFormatException extends RuntimeException {

    private static final long serialVersionUID = 5725714637732803589L;

    public UnsupportedDataSetFormatException(final String message) {
        super(message);
    }
}
