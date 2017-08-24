package eu.drus.jpa.unit.cassandra.dataset;

import java.io.IOException;
import java.io.InputStream;

public interface DataSetParser {

    void setContentHandler(ContentHandler handler);

    void parse(InputStream in) throws IOException;
}
