package eu.drus.jpa.unit.cassandra.operation;

import static eu.drus.jpa.unit.util.ResourceLocator.getResource;

import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Test;
import org.mockito.Mockito;

import eu.drus.jpa.unit.cassandra.dataset.ContentHandler;
import eu.drus.jpa.unit.cassandra.dataset.ParsedDataSet;
import eu.drus.jpa.unit.cassandra.dataset.json.JsonDataSetParser;

public class JsonDataSetReaderTest {

    @Test
    public void testParse() throws IOException {
        final FileInputStream in = new FileInputStream(getResource("test.json").getPath());

        final JsonDataSetParser reader = new JsonDataSetParser();
        reader.setContentHandler(Mockito.mock(ContentHandler.class));
        reader.parse(in);
    }

    @Test
    public void testParseDS() throws IOException {
        final FileInputStream in = new FileInputStream(getResource("test.json").getPath());

        final JsonDataSetParser reader = new JsonDataSetParser();
        final ParsedDataSet ds = new ParsedDataSet();
        reader.setContentHandler(ds);
        reader.parse(in);

        ds.hashCode();
    }

}
