package eu.drus.jpa.unit.sql.dbunit.ext;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
import org.hamcrest.core.Is;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Oracle10ConnectionFactoryTest {
    private static final DbUnitConnectionFactory FACTORY = new Oracle10ConnectionFactory();

    @Test
    public void testDriverClassSupport() {
        assertTrue(FACTORY.supportsDriver("oracle.jdbc.OracleDriver"));
        assertTrue(FACTORY.supportsDriver("oracle.jdbc.driver.OracleDriver"));
    }

    @Test
    @Ignore("oracle jdbc required in classpath")
    public void testCreateConnectionNoSchemaAvailable() throws DatabaseUnitException, SQLException {
        // WHEN
        Connection connectionMock = mock(Connection.class);
        when(connectionMock.getSchema()).thenThrow(SQLException.class);
        
        final IDatabaseConnection connection = FACTORY.createConnection(connectionMock);

        // THEN
        assertThat(connection, notNullValue());
        assertThat(connection.getSchema(), nullValue());
        
        final Object typeFactory = connection.getConfig().getProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY);
        assertThat(typeFactory, notNullValue());
        assertThat(typeFactory.getClass(), not(equalTo(DefaultDataTypeFactory.class)));
    }
    
    
    @Test
    @Ignore("oracle jdbc required in classpath")
    public void testCreateConnectionSchemaAvailableFromConnection() throws SQLException, DatabaseUnitException {
        // WHEN
        Connection connectionMock = mock(Connection.class);
        when(connectionMock.getSchema()).thenReturn("schemaName");
        DatabaseMetaData dbMetadataMock = mock(DatabaseMetaData.class);
        when(dbMetadataMock.getIdentifierQuoteString()).thenReturn("'");
        
        when(connectionMock.getMetaData()).thenReturn(dbMetadataMock);
        
        // we Use same resultset mock for both catalog and schema proofing
        ResultSet resultSetMock = mock(ResultSet.class);
        doNothing().when(resultSetMock).close();
        
        when(dbMetadataMock.getCatalogs()).thenReturn(resultSetMock);
        when(dbMetadataMock.getSchemas()).thenReturn(resultSetMock);
        
        final IDatabaseConnection connection = FACTORY.createConnection(connectionMock);
    
        // THEN
        assertThat(connection, notNullValue());
        assertThat(connection.getSchema(), Is.is("schemaName"));
    
        final Object typeFactory = connection.getConfig().getProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY);
        assertThat(typeFactory, notNullValue());
        assertThat(typeFactory.getClass(), not(equalTo(DefaultDataTypeFactory.class)));
    }
}
