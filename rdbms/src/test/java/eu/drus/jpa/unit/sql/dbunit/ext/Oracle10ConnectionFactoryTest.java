package eu.drus.jpa.unit.sql.dbunit.ext;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.util.SQLHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Oracle10ConnectionFactoryTest {

    private static final DbUnitConnectionFactory FACTORY = new Oracle10ConnectionFactory();

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData metaData;

    private MockedStatic<SQLHelper> sqlHelperMock;

    @Before
    public void repareMocks() throws SQLException {
        sqlHelperMock = mockStatic(SQLHelper.class);
        sqlHelperMock.when(() -> SQLHelper.correctCase(anyString(), any(Connection.class))).then(invocation -> {
            return invocation.getArguments()[0];
        });
        sqlHelperMock.when(() -> SQLHelper.schemaExists(any(Connection.class), anyString())).thenReturn(Boolean.TRUE);

        lenient().when(connection.getMetaData()).thenReturn(metaData);
        lenient().when(metaData.getIdentifierQuoteString()).thenReturn(" ");
    }

    @After
    public void releaseMocks() {
        sqlHelperMock.close();
    }

    @Test
    public void testDriverClassSupport() {
        assertTrue(FACTORY.supportsDriver("oracle.jdbc.OracleDriver"));
        assertTrue(FACTORY.supportsDriver("oracle.jdbc.driver.OracleDriver"));
    }

    @Test
    @Ignore("oracle jdbc required in classpath")
    public void testCreateConnection() throws DatabaseUnitException, SQLException {
        // GIVEN
        final String schema = "foo";

        // WHEN
        final IDatabaseConnection dbConnection = FACTORY.createConnection(connection, schema);

        // THEN
        assertThat(dbConnection, notNullValue());

        final Object typeFactory = dbConnection.getConfig().getProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY);
        assertThat(typeFactory, notNullValue());
        assertThat(typeFactory.getClass(), equalTo(Oracle10DataTypeFactory.class));

        assertThat(dbConnection.getSchema(), equalTo(schema));
    }
}
