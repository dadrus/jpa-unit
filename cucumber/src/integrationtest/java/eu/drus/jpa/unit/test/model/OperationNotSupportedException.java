package eu.drus.jpa.unit.test.model;

public class OperationNotSupportedException extends Exception {

    private static final long serialVersionUID = -7893771455528048183L;

    public OperationNotSupportedException(final String msg) {
        super(msg);
    }
}
