package eu.drus.test.persistence.core.dbunit;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
import org.dbunit.dataset.datatype.IDataTypeFactory;
import org.dbunit.ext.db2.Db2DataTypeFactory;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.ext.mckoi.MckoiDataTypeFactory;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.ext.netezza.NetezzaDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;

import eu.drus.test.persistence.JpaUnitException;

public class DatabaseConnectionFactory {

    private Map<String, Object> properties;

    public DatabaseConnectionFactory(final Map<String, Object> properties) {
        this.properties = properties;
    }

    public IDatabaseConnection openConnection() {
        final String driverClass = (String) properties.get("javax.persistence.jdbc.driver");
        final String connectionUrl = (String) properties.get("javax.persistence.jdbc.url");
        final String username = (String) properties.get("javax.persistence.jdbc.user");
        final String password = (String) properties.get("javax.persistence.jdbc.password");

        loadDriver(driverClass);

        final IDatabaseConnection connection = createDBUnitDatabaseConnection(connectionUrl, username, password);
        final DatabaseConfig config = connection.getConfig();
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, determineDataTypeFactory(driverClass));

        return connection;
    }

    private IDataTypeFactory determineDataTypeFactory(final String driverClass) {
        switch (driverClass) {
        case "com.ibm.db2.jcc.DB2Driver":
            return new Db2DataTypeFactory();
        case "org.h2.Driver":
            return new H2DataTypeFactory();
        case "org.hsqldb.jdbc.JDBCDriver":
        case "org.hsqldb.jdbcDriver":
            return new HsqldbDataTypeFactory();
        case "com.mckoi.JDBCDriver":
            return new MckoiDataTypeFactory();
        case "com.microsoft.sqlserver.jdbc.SQLServerDriver":
            return new MsSqlDataTypeFactory();
        case "com.mysql.jdbc.Driver":
            return new MySqlDataTypeFactory();
        case "org.netezza.Driver":
            return new NetezzaDataTypeFactory();
        case "oracle.jdbc.OracleDriver":
        case "oracle.jdbc.driver.OracleDriver":
            return new Oracle10DataTypeFactory();
        case "org.postgresql.Driver":
            return new PostgresqlDataTypeFactory();
        default:
            return new DefaultDataTypeFactory();
        }
    }

    private DatabaseConnection createDBUnitDatabaseConnection(final String connectionUrl, final String username, final String password) {
        try {
            if (username == null && password == null) {
                return new DatabaseConnection(DriverManager.getConnection(connectionUrl));
            } else {
                return new DatabaseConnection(DriverManager.getConnection(connectionUrl, username, password));
            }
        } catch (DatabaseUnitException | SQLException e) {
            throw new JpaUnitException(e);
        }
    }

    private void loadDriver(final String driverClass) {
        try {
            Class.forName(driverClass);
        } catch (final ClassNotFoundException e) {
            throw new JpaUnitException(e);
        }
    }
}
