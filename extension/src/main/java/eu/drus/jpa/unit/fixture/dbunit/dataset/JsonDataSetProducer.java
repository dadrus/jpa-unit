package eu.drus.jpa.unit.fixture.dbunit.dataset;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import org.dbunit.dataset.DataSetException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonDataSetProducer extends DataSetProducer {

    private Gson gson;

    public JsonDataSetProducer(final InputStream input) {
        super(input);
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Map<String, List<Map<String, String>>> loadDataSet() throws DataSetException {
        try {
            return gson.fromJson(new InputStreamReader(input), Map.class);
        } catch (final Exception e) {
            throw new DataSetException("Error parsing json data set", e);
        }
    }

}
