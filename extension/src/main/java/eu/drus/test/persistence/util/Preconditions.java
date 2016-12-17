package eu.drus.test.persistence.util;

public final class Preconditions {
    private Preconditions() {}

    public static void checkArgument(final boolean flag, final String msg) {
        if (!flag) {
            throw new IllegalArgumentException(msg);
        }
    }
}
