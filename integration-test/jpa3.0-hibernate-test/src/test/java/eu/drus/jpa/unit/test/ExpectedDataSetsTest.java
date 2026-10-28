package eu.drus.jpa.unit.test;

import java.util.Arrays;
import java.util.List;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.filter.IColumnFilter;
import org.junit.Test;
import org.junit.runner.RunWith;

import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.api.JpaUnitRunner;
import eu.drus.jpa.unit.test.model.OperationNotSupportedException;

@RunWith(JpaUnitRunner.class)
public class ExpectedDataSetsTest extends AbstractExpectedDataSetsTest {

    public static class CustomnColumnFilter implements IColumnFilter {

        private static List<String> names = Arrays.asList("ID", "DEPOSITOR_ID", "ACCOUNT_ID", "VERSION");

        @Override
        public boolean accept(final String tableName, final Column column) {
            return !names.contains(column.getColumnName());
        }

    }

    @Test
    @ExpectedDataSets(value = "datasets/expected-data.json", orderBy = {
            "CONTACT_DETAIL.TYPE", "ACCOUNT_ENTRY.TYPE"
    }, filter = CustomnColumnFilter.class)
    public void test5() throws OperationNotSupportedException {
        // we can also apply specific filters instead of excluding columns
        manager.persist(depositor);
    }
}
