package eu.drus.test.persistence.core.dbunit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.apache.commons.codec.Charsets;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.h2.tools.RunScript;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CleanupStrategyProviderTest {

    public static final String DRIVER_CLASS = "org.h2.Driver";
    public static final String CONNECTION_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    public static final String USER_NAME = "test";
    public static final String PASSWORD = "test";
    public static final String DB_SETUP_SCRIPT = "src/test/resources/schema.sql";

    @BeforeClass
    public static void createSchema() throws Exception {
        RunScript.execute(CONNECTION_URL, USER_NAME, PASSWORD, DB_SETUP_SCRIPT, Charsets.UTF_8, false);
    }

    private DatabaseConnection connection;
    private CleanupStrategyProvider provider;

    private int getRecordCountFromTable(final DatabaseConnection connection, final String tableName) throws SQLException {
        final ResultSet rs = connection.getConnection().createStatement().executeQuery("select count(*) from " + tableName + ";");
        rs.next();
        return rs.getInt(1);
    }

    @Before
    public void setUp() throws Exception {
        final IDataSet initialDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/test-data.xml"));
        connection = new DatabaseConnection(DriverManager.getConnection(CONNECTION_URL, USER_NAME, PASSWORD));

        final DatabaseOperation operation = DatabaseOperation.CLEAN_INSERT;
        operation.execute(connection, initialDataSet);

        connection.getConnection().createStatement().execute(
                "insert into XML_TABLE_1(id, version, value_1, value_2, value_3, value_4, value_5) values(10, 'Record 10 version', 'Record 10 Value 1', 'Record 10 Value 2', 'Record 10 Value 3', 'Record 10 Value 4', 'Record 10 Value 5');");
        connection.getConnection().commit();

        connection.getConnection().createStatement().execute(
                "merge into XML_TABLE_3(id, version, value_8, value_9) values(11, 'Record 11 version', 'Record 11 Value 8', 'Record 11 Value 9');");
        connection.getConnection().commit();

        assertThat(getRecordCountFromTable(connection, "XML_TABLE_1"), equalTo(4));
        assertThat(getRecordCountFromTable(connection, "XML_TABLE_2"), equalTo(1));
        assertThat(getRecordCountFromTable(connection, "XML_TABLE_2"), equalTo(1));
        connection.close();

        connection = new DatabaseConnection(DriverManager.getConnection(CONNECTION_URL, USER_NAME, PASSWORD));
        provider = new CleanupStrategyProvider(connection, Arrays.asList(initialDataSet));
    }

    public void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    public void testStrictCleanup() throws Exception {
        // GIVEN
        final CleanupStrategyExecutor strategyExecutor = provider.strictStrategy();
        assertThat(strategyExecutor, notNullValue());

        // WHEN
        strategyExecutor.cleanupDatabase();

        // THEN
        assertThat(getRecordCountFromTable(connection, "XML_TABLE_1"), equalTo(0));
        assertThat(getRecordCountFromTable(connection, "XML_TABLE_2"), equalTo(0));
        assertThat(getRecordCountFromTable(connection, "XML_TABLE_3"), equalTo(0));
    }

    @Test
    public void testStrictCleanupExcludingOneTable() throws Exception {
        // GIVEN
        final CleanupStrategyExecutor strategyExecutor = provider.strictStrategy();
        assertThat(strategyExecutor, notNullValue());

        // WHEN
        strategyExecutor.cleanupDatabase("XML_TABLE_2");

        // THEN
        assertThat(getRecordCountFromTable(connection, "XML_TABLE_1"), equalTo(0));
        assertThat(getRecordCountFromTable(connection, "XML_TABLE_2"), equalTo(1));
        assertThat(getRecordCountFromTable(connection, "XML_TABLE_3"), equalTo(0));
    }

    @Test
    public void testUsedRowsOnlyCleanup() throws Exception {
        // GIVEN
        final CleanupStrategyExecutor strategyExecutor = provider.usedRowsOnlyStrategy();
        assertThat(strategyExecutor, notNullValue());

        // WHEN
        strategyExecutor.cleanupDatabase();

        // THEN
        assertThat(getRecordCountFromTable(connection, "XML_TABLE_1"), equalTo(1));
        assertThat(getRecordCountFromTable(connection, "XML_TABLE_2"), equalTo(0));
        assertThat(getRecordCountFromTable(connection, "XML_TABLE_3"), equalTo(1));
    }

    @Test
    public void testUsedRowsOnlyCleanupExcludingOneTable() throws Exception {
        // GIVEN
        final CleanupStrategyExecutor strategyExecutor = provider.usedRowsOnlyStrategy();
        assertThat(strategyExecutor, notNullValue());

        // WHEN
        strategyExecutor.cleanupDatabase("XML_TABLE_2");

        // THEN
        assertThat(getRecordCountFromTable(connection, "XML_TABLE_1"), equalTo(1));
        assertThat(getRecordCountFromTable(connection, "XML_TABLE_2"), equalTo(1));
        assertThat(getRecordCountFromTable(connection, "XML_TABLE_3"), equalTo(1));
    }

    @Test
    public void testUsedTablesOnlyCleanup() throws Exception {
        // GIVEN
        final CleanupStrategyExecutor strategyExecutor = provider.usedTablesOnlyStrategy();
        assertThat(strategyExecutor, notNullValue());

        // WHEN
        strategyExecutor.cleanupDatabase();

        // THEN
        assertThat(getRecordCountFromTable(connection, "XML_TABLE_1"), equalTo(0));
        assertThat(getRecordCountFromTable(connection, "XML_TABLE_2"), equalTo(0));
        assertThat(getRecordCountFromTable(connection, "XML_TABLE_3"), equalTo(1));
    }

    @Test
    public void testUsedTablesOnlyCleanupExcludingOneTable() throws Exception {
        // GIVEN
        final CleanupStrategyExecutor strategyExecutor = provider.usedTablesOnlyStrategy();
        assertThat(strategyExecutor, notNullValue());

        // WHEN
        strategyExecutor.cleanupDatabase("XML_TABLE_2");

        // THEN
        assertThat(getRecordCountFromTable(connection, "XML_TABLE_1"), equalTo(0));
        assertThat(getRecordCountFromTable(connection, "XML_TABLE_2"), equalTo(1));
        assertThat(getRecordCountFromTable(connection, "XML_TABLE_3"), equalTo(1));
    }
}
