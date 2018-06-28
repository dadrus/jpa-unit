package eu.drus.jpa.unit.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;

import org.junit.Test;

public class ReflectionUtilsTest {

    private Long privateField;

    @Test
    public void testInjectPrivateFieldUsingFieldObject() throws Exception {
        // GIVEN
        final Long value = 10L;
        final Field toi = getClass().getDeclaredField("privateField");

        // WHEN
        ReflectionUtils.injectValue(this, toi, value);

        // THEN
        assertThat(privateField, equalTo(value));
    }
    
    @Test
    public void testInjectPrivateFieldUsingFieldName() throws Exception {
        // GIVEN
        final Long value = 10L;

        // WHEN
        ReflectionUtils.injectValue(this, "privateField", value);

        // THEN
        assertThat(privateField, equalTo(value));
    }

    @Test
    public void testGetValeOfAPrivateFieldUsingFieldObject() throws Exception {
        // GIVEN
        privateField = 10l;
        final Field toi = getClass().getDeclaredField("privateField");

        // WHEN
        final Object value = ReflectionUtils.getValue(this, toi);

        // THEN
        assertThat(privateField, equalTo(value));
    }
    
    @Test
    public void testGetValeOfAPrivateFieldUsingFieldName() throws Exception {
        // GIVEN
        privateField = 10l;

        // WHEN
        final Object value = ReflectionUtils.getValue(this, "privateField");

        // THEN
        assertThat(privateField, equalTo(value));
    }
}
