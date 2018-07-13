/*
 *
 */
package eu.drus.jpa.unit.sql.dbunit.ext;

import java.sql.Connection;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;

public interface DbUnitConnectionFactory {

    boolean supportsDriver(final String driverClass);

    IDatabaseConnection createConnection(final Connection connection, final String schema) throws DatabaseUnitException;
}
