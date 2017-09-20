package eu.drus.jpa.unit.core;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;

public class PersistenceUnitDescriptorLoaderTest {

    @Test
    public void testPersistenceUnitDescriptorLoader() throws IOException {
        // GIVEN
        final PersistenceUnitDescriptorLoader loader = new PersistenceUnitDescriptorLoader();

        // WHEN
        final List<PersistenceUnitDescriptor> descriptors = loader.loadPersistenceUnitDescriptors(Collections.emptyMap());

        // THEN
        assertThat(descriptors, notNullValue());
        assertThat(descriptors.size(), equalTo(4));
        assertThat(descriptors, not(hasItem(nullValue())));
    }
}
