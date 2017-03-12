package eu.drus.jpa.unit.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.drus.jpa.unit.util.Preconditions;

public class PreconditionsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testCheckArgumentForFalseCondition() {
        // EXPECTATIONS
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("some message");

        // WHEN
        Preconditions.checkArgument(false, "some message");
    }

    @Test
    public void testCheckArgumentForTrueCondition() {
        // WHEN
        Preconditions.checkArgument(true, "some message");

        // THEN
        // nothing will happen
    }
}
