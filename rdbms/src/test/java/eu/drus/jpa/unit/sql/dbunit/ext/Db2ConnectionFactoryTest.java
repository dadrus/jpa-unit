package eu.drus.jpa.unit.sql.dbunit.ext;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.db2.Db2DataTypeFactory;
import org.dbunit.ext.db2.Db2MetadataHandler;
import org.dbunit.util.SQLHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SQLHelper.class)
public class Db2ConnectionFactoryTest {

    private static final DbUnitConnectionFactory FACTORY = new Db2ConnectionFactory();

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData metaData;

    @Before
    public void repareMocks() throws SQLException {
        mockStatic(SQLHelper.class);
        when(SQLHelper.correctCase(anyString(), any(Connection.class))).then(invocation -> {
            return invocation.getArguments()[0];
        });
        when(SQLHelper.schemaExists(any(Connection.class), anyString())).thenReturn(Boolean.TRUE);

        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getIdentifierQuoteString()).thenReturn(" ");
    }

    @Test
    public void testDriverClassSupport() {
        assertTrue(FACTORY.supportsDriver("com.ibm.db2.jcc.DB2Driver"));
    }

    @Test
    public void testCreateConnection() throws Exception {
        // GIVEN
        final String schema = "foo";

        // WHEN
        final IDatabaseConnection dbConnection = FACTORY.createConnection(connection, schema);

        // THEN
        assertThat(dbConnection, notNullValue());

        final Object typeFactory = dbConnection.getConfig().getProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY);
        assertThat(typeFactory, notNullValue());
        assertThat(typeFactory.getClass(), equalTo(Db2DataTypeFactory.class));

        final Object metadataHandler = dbConnection.getConfig().getProperty(DatabaseConfig.PROPERTY_METADATA_HANDLER);
        assertThat(metadataHandler, notNullValue());
        assertThat(metadataHandler.getClass(), equalTo(Db2MetadataHandler.class));

        assertThat(dbConnection.getSchema(), equalTo(schema));
    }
}
