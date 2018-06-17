package eu.drus.jpa.unit.sql;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;

import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;

@RunWith(MockitoJUnitRunner.class)
public class SqlDbConfigurationTest {

	@Mock
	private PersistenceUnitDescriptor descriptor;

	@Test
	public void testPersistenceUnitDescriptorIsSupported() {
		// GIVEN
		when(descriptor.getProperties()).thenReturn(ImmutableMap.<String, Object>builder()
				.put("javax.persistence.jdbc.driver", "some.Driver")
				.put("javax.persistence.jdbc.url", "some://url")
				.build());

		// WHEN
		boolean isSupported = SqlDbConfiguration.isSupported(descriptor);

		// THEN
		assertTrue(isSupported);
	}

	@Test
	public void testPersistenceUnitDescriptorIsNotSupportedIfDriverPropertyIsMissing() {
		// GIVEN
		when(descriptor.getProperties()).thenReturn(
				ImmutableMap.<String, Object>builder().put("javax.persistence.jdbc.url", "some://url").build());

		// WHEN
		boolean isSupported = SqlDbConfiguration.isSupported(descriptor);

		// THEN
		assertFalse(isSupported);
	}

	@Test
	public void testPersistenceUnitDescriptorIsNotSupportedIfUrlPropertyIsMissing() {
		// GIVEN
		when(descriptor.getProperties()).thenReturn(
				ImmutableMap.<String, Object>builder().put("javax.persistence.jdbc.driver", "some.Driver").build());

		// WHEN
		boolean isSupported = SqlDbConfiguration.isSupported(descriptor);

		// THEN
		assertFalse(isSupported);
	}

	@Test
	public void testCreateDataSource() {
		// GIVEN
		when(descriptor.getProperties()).thenReturn(ImmutableMap.<String, Object>builder()
			.put("javax.persistence.jdbc.driver", "some.Driver")
			.put("javax.persistence.jdbc.url", "some://url")
			.put("javax.persistence.jdbc.user", "someUser")
			.put("javax.persistence.jdbc.password", "somePassword")
			.build());

		SqlDbConfiguration config = new SqlDbConfiguration(descriptor);
		
		
		// WHEN
		BasicDataSource dataSource = (BasicDataSource) config.createDataSource();
		
		// THEN
		assertNotNull(dataSource);
		assertThat(dataSource.getDriverClassName(),equalTo("some.Driver"));
		assertThat(dataSource.getUrl(),equalTo("some://url"));
		assertThat(dataSource.getUsername(),equalTo("someUser"));
		assertThat(dataSource.getPassword(),equalTo("somePassword"));
		assertThat(dataSource.getMinIdle(),equalTo(1));
		assertThat(dataSource.getMaxIdle(),equalTo(2));
	}
}
