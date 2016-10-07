package eu.drus.test.persistence.core.dbunit.dataset;

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
    }

    private Gson getGson() {
        if (gson == null) {
            final GsonBuilder gsonBuilder = new GsonBuilder();
            gson = gsonBuilder.create();
        }
        return gson;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Map<String, List<Map<String, String>>> loadDataSet() throws DataSetException {
        try {
            return getGson().fromJson(new InputStreamReader(input), Map.class);
        } catch (final Exception e) {
            throw new DataSetException("Error parsing json data set", e);
        }
    }

}
