package eu.drus.jpa.unit.util;

public final class Preconditions {
    private Preconditions() {}

    public static void checkArgument(final boolean flag, final String msg) {
        if (!flag) {
            throw new IllegalArgumentException(msg);
        }
    }
}
