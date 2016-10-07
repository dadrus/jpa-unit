package eu.drus.test.persistence.core.dbunit.dataset;

import static eu.drus.test.persistence.core.dbunit.dataset.ColumnNameMatcher.columnWithName;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.dbunit.dataset.stream.IDataSetProducer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.gson.JsonSyntaxException;

public class JsonDataSetProducerTest {

    private InputStream jsonStream;
    private InputStream yamlStream;

    @Before
    public void openResourceStream() {
        jsonStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test-data.json");
        yamlStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test-data.yaml");
    }

    @After
    public void closeResourceStream() throws IOException {
        jsonStream.close();
        yamlStream.close();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProduceDataSetUsingValidStream() throws DataSetException {
        // GIVEN
        final IDataSetConsumer consumer = mock(IDataSetConsumer.class);
        final IDataSetProducer producer = new JsonDataSetProducer(jsonStream);
        producer.setConsumer(consumer);

        // WHEN
        producer.produce();

        // THEN
        verify(consumer).startDataSet();

        final ArgumentCaptor<ITableMetaData> tmdCaptor = ArgumentCaptor.forClass(ITableMetaData.class);
        verify(consumer, times(2)).startTable(tmdCaptor.capture());

        final List<ITableMetaData> tmdList = tmdCaptor.getAllValues();
        final ITableMetaData tmd1 = tmdList.get(0);
        final List<Column> table1Columns = Arrays.asList(tmd1.getColumns());
        final ITableMetaData tmd2 = tmdList.get(1);
        final List<Column> table2Columns = Arrays.asList(tmd2.getColumns());

        assertThat(tmd1.getTableName(), equalTo("TABLE_1"));
        assertThat(table1Columns.size(), equalTo(7));
        assertThat(table1Columns, hasItems(columnWithName("id"), columnWithName("version"), columnWithName("value_1"),
                columnWithName("value_2"), columnWithName("value_3"), columnWithName("value_4"), columnWithName("value_5")));

        assertThat(tmd2.getTableName(), equalTo("TABLE_2"));
        assertThat(table2Columns.size(), equalTo(4));
        assertThat(table2Columns,
                hasItems(columnWithName("id"), columnWithName("version"), columnWithName("value_6"), columnWithName("value_7")));

        final ArgumentCaptor<Object[]> rowCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(consumer, times(4)).row(rowCaptor.capture());

        final List<Object[]> allRows = rowCaptor.getAllValues();

        final Map<String, String> record1 = rebuildRecord(table1Columns, allRows.get(0));
        assertThat(record1.size(), equalTo(7));
        assertThat(record1.get("id"), equalTo("Record 1"));
        assertThat(record1.get("version"), equalTo("Record 1 version"));
        assertThat(record1.get("value_1"), equalTo("Record 1 Value 1"));
        assertThat(record1.get("value_2"), equalTo("Record 1 Value 2"));
        assertThat(record1.get("value_3"), nullValue());
        assertThat(record1.get("value_4"), equalTo("Record 1 Value 4"));
        assertThat(record1.get("value_5"), nullValue());

        final Map<String, String> record2 = rebuildRecord(table1Columns, allRows.get(1));
        assertThat(record2.size(), equalTo(7));
        assertThat(record2.get("id"), equalTo("Record 2"));
        assertThat(record2.get("version"), equalTo("Record 2 version"));
        assertThat(record2.get("value_1"), equalTo("Record 2 Value 1"));
        assertThat(record2.get("value_2"), equalTo("Record 2 Value 2"));
        assertThat(record2.get("value_3"), equalTo("Record 2 Value 3"));
        assertThat(record2.get("value_4"), nullValue());
        assertThat(record2.get("value_5"), nullValue());

        final Map<String, String> record3 = rebuildRecord(table1Columns, allRows.get(2));
        assertThat(record3.size(), equalTo(7));
        assertThat(record3.get("id"), equalTo("Record 3"));
        assertThat(record3.get("version"), equalTo("Record 3 version"));
        assertThat(record3.get("value_1"), nullValue());
        assertThat(record3.get("value_2"), nullValue());
        assertThat(record3.get("value_3"), nullValue());
        assertThat(record3.get("value_4"), nullValue());
        assertThat(record3.get("value_5"), equalTo("Record 3 Value 5"));

        final Map<String, String> record4 = rebuildRecord(table2Columns, allRows.get(3));
        assertThat(record4.size(), equalTo(4));
        assertThat(record4.get("id"), equalTo("Record 4"));
        assertThat(record4.get("version"), equalTo("Record 4 version"));
        assertThat(record4.get("value_6"), equalTo("Record 4 Value 6"));
        assertThat(record4.get("value_7"), equalTo("Record 4 Value 7"));

        verify(consumer, times(2)).endTable();

        verify(consumer).endDataSet();
    }

    @Test
    public void testProduceDataSetUsingNullStream() throws DataSetException {
        // GIVEN
        final IDataSetConsumer consumer = mock(IDataSetConsumer.class);
        final IDataSetProducer producer = new JsonDataSetProducer(null);
        producer.setConsumer(consumer);

        // WHEN
        try {
            producer.produce();
            fail("DataSetException expected");
        } catch (final DataSetException e) {
            // THEN
            assertThat(e.getCause(), instanceOf(NullPointerException.class));
        }
    }

    @Test
    public void testProduceDataSetUsingNotJsonStream() throws DataSetException {
        // GIVEN
        final IDataSetConsumer consumer = mock(IDataSetConsumer.class);
        final IDataSetProducer producer = new JsonDataSetProducer(yamlStream);
        producer.setConsumer(consumer);

        // WHEN
        try {
            producer.produce();
            fail("DataSetException expected");
        } catch (final DataSetException e) {
            // THEN
            assertThat(e.getCause(), instanceOf(JsonSyntaxException.class));
        }
    }

    private Map<String, String> rebuildRecord(final List<Column> columns, final Object[] entries) {
        final Map<String, String> record = new HashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            record.put(columns.get(i).getColumnName(), (String) entries[i]);
        }
        return record;
    }
}
