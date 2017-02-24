package eu.drus.jpa.unit.fixture.dbunit.ext;

import java.sql.Connection;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;

public interface DbUnitConnectionFactory {

    boolean supportsDriver(String driverClass);

    IDatabaseConnection createConnection(Connection connection) throws DatabaseUnitException;
}
