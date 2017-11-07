package eu.drus.jpa.unit.neo4j.dataset;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import eu.drus.jpa.unit.neo4j.dataset.graphml.AttributeType;
import eu.drus.jpa.unit.neo4j.dataset.graphml.DefaultAttribute;

public class AttributeTypeConverterTest {

    @Test
    public void testConvertBoolean() {
        // GIVEN
        final DefaultAttribute<Boolean> attribute = new DefaultAttribute<>(true, AttributeType.BOOLEAN);

        // WHEN
        final Object value = AttributeTypeConverter.convert(attribute);

        // THEN
        assertThat(value, instanceOf(Boolean.class));
        assertThat(value, equalTo(Boolean.TRUE));
    }

    @Test
    public void testConvertFloat() {
        // GIVEN
        final DefaultAttribute<Float> attribute = new DefaultAttribute<>(10.0f, AttributeType.FLOAT);

        // WHEN
        final Object value = AttributeTypeConverter.convert(attribute);

        // THEN
        assertThat(value, instanceOf(Double.class));
        assertThat(value, equalTo(10.0));
    }

    @Test
    public void testConvertDouble() {
        // GIVEN
        final DefaultAttribute<Double> attribute = new DefaultAttribute<>(10.0, AttributeType.DOUBLE);

        // WHEN
        final Object value = AttributeTypeConverter.convert(attribute);

        // THEN
        assertThat(value, instanceOf(Double.class));
        assertThat(value, equalTo(10.0));
    }

    @Test
    public void testConvertInteger() {
        // GIVEN
        final DefaultAttribute<Integer> attribute = new DefaultAttribute<>(10, AttributeType.INT);

        // WHEN
        final Object value = AttributeTypeConverter.convert(attribute);

        // THEN
        assertThat(value, instanceOf(Long.class));
        assertThat(value, equalTo(10l));
    }

    @Test
    public void testConvertLong() {
        // GIVEN
        final DefaultAttribute<Long> attribute = new DefaultAttribute<>(10l, AttributeType.LONG);

        // WHEN
        final Object value = AttributeTypeConverter.convert(attribute);

        // THEN
        assertThat(value, instanceOf(Long.class));
        assertThat(value, equalTo(10l));
    }

    @Test
    public void testConvertString() {
        // GIVEN
        final DefaultAttribute<String> attribute = new DefaultAttribute<>("foo", AttributeType.STRING);

        // WHEN
        final Object value = AttributeTypeConverter.convert(attribute);

        // THEN
        assertThat(value, instanceOf(String.class));
        assertThat(value, equalTo("foo"));
    }
}
