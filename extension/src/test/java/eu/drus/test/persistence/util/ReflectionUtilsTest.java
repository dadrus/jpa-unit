package eu.drus.test.persistence.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;

import org.junit.Test;

public class ReflectionUtilsTest {

    private Long privateField;

    @Test
    public void testInjectPrivateField() throws Exception {
        // GIVEN
        final Long value = 10L;
        final Field toi = getClass().getDeclaredField("privateField");

        // WHEN
        ReflectionUtils.injectValue(toi, this, value);

        // THEN
        assertThat(privateField, equalTo(value));
    }
}
