package eu.drus.jpa.unit.spi;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import eu.drus.jpa.unit.spi.AssertionErrorCollector;

public class AssertionErrorCollectorTest {

    private static final String ERROR_TEXT = "some text";

    private AssertionErrorCollector collector;

    @Before
    public void setUp() {
        collector = new AssertionErrorCollector();
    }

    @Test
    public void testReportAfterCollectingSingleAssertionError() {
        final AssertionError error = new AssertionError(ERROR_TEXT);

        collector.collect(error);

        assertThat(collector.amountOfErrors(), equalTo(1));

        try {
            collector.report();
        } catch (final AssertionError e) {
            assertThat(e.getMessage(), containsString("Test failed in 1 case"));
            assertThat(e.getMessage(), containsString(ERROR_TEXT));
        }
    }

    @Test
    public void testReportAfterCollectingSingleAssertionMessage() {
        collector.collect(ERROR_TEXT);

        assertThat(collector.amountOfErrors(), equalTo(1));

        try {
            collector.report();
        } catch (final AssertionError e) {
            assertThat(e.getMessage(), containsString("Test failed in 1 case"));
            assertThat(e.getMessage(), containsString(ERROR_TEXT));
        }
    }

    @Test
    public void testReportAfterCollectingMultipleAssertions() {
        collector.collect(ERROR_TEXT);
        collector.collect(ERROR_TEXT);

        assertThat(collector.amountOfErrors(), equalTo(2));

        try {
            collector.report();
        } catch (final AssertionError e) {
            assertThat(e.getMessage(), containsString("Test failed in 2 cases"));
            assertThat(e.getMessage(), containsString(ERROR_TEXT));
        }
    }

    @Test
    public void testReportWithoutCollectingAssertions() {
        assertThat(collector.amountOfErrors(), equalTo(0));

        collector.report();
    }
}
