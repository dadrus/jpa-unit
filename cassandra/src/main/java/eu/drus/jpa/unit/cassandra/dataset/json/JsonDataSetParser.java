package eu.drus.jpa.unit.cassandra.dataset.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import eu.drus.jpa.unit.cassandra.dataset.Column;
import eu.drus.jpa.unit.cassandra.dataset.ContentHandler;
import eu.drus.jpa.unit.cassandra.dataset.DataSetParser;
import eu.drus.jpa.unit.cassandra.dataset.DefaultRowElement;
import eu.drus.jpa.unit.cassandra.dataset.DefaultTableProperties;
import eu.drus.jpa.unit.cassandra.dataset.RowElement;
import eu.drus.jpa.unit.cassandra.dataset.TableProperties;

public class JsonDataSetParser implements DataSetParser {

    private ContentHandler handler;

    private Deque<Object> deque = new ArrayDeque<>();

    @Override
    public void setContentHandler(final ContentHandler handler) {
        this.handler = handler;
    }

    @Override
    public void parse(final InputStream in) throws IOException {
        try (final JsonReader reader = new JsonReader(new InputStreamReader(in))) {
            parse(reader);
        }
    }

    private void parse(final JsonReader reader) throws IOException {
        onStartDocument();
        while (true) {
            final JsonToken token = reader.peek();
            switch (token) {
            case BEGIN_OBJECT:
                reader.beginObject();
                onBeginObject();
                break;
            case END_OBJECT:
                reader.endObject();
                onEndObject();
                break;
            case BEGIN_ARRAY:
                reader.beginArray();
                onBeginArray();
                break;
            case END_ARRAY:
                reader.endArray();
                onEndArray();
                break;
            case NULL:
                reader.nextNull();
                onNull();
                break;
            case NAME:
                final String name = reader.nextName();
                onName(name);
                break;
            case STRING:
                onValue(reader.nextString());
                break;
            case BOOLEAN:
                onValue(reader.nextBoolean());
                break;
            case NUMBER:
                final String n = reader.nextString();
                if (n.contains(".")) {
                    onValue(Double.valueOf(n));
                } else {
                    onValue(Long.valueOf(n));
                }

                break;
            case END_DOCUMENT:
                onEndDocument();
                return;
            }
        }
    }

    private void onStartDocument() {
        handler.onDataSetStart();
    }

    private void onEndDocument() {
        handler.onDataSetEnd();
    }

    private void onBeginObject() {
        final Object obj = deque.peek();
        if (obj != null) {
            deque.push(new ArrayList<RowElement>());
        }
    }

    @SuppressWarnings("unchecked")
    private void onEndObject() {
        final Object obj = deque.poll();
        if (obj instanceof List) {
            // row complete
            handler.onRow((List<RowElement>) obj);
        }
    }

    private void onBeginArray() {
        handler.onTableStart((TableProperties) deque.peek());
    }

    private void onEndArray() {
        handler.onTableEnd();
        deque.pop();
    }

    @SuppressWarnings("unchecked")
    private void onNull() {
        final Column column = (Column) deque.pop();
        final List<RowElement> row = (List<RowElement>) deque.peek();
        row.add(new DefaultRowElement(column, null));
    }

    @SuppressWarnings("unchecked")
    private void onValue(final Object value) {
        final Column column = (Column) deque.pop();
        final List<RowElement> row = (List<RowElement>) deque.peek();
        row.add(new DefaultRowElement(column, value));
    }

    @SuppressWarnings("unchecked")
    private void onValue(final Long value) {
        final Column column = (Column) deque.pop();
        final List<RowElement> row = (List<RowElement>) deque.peek();
        row.add(new DefaultRowElement(column, value));
    }

    private void onName(final String name) {
        final Object obj = deque.poll();
        if (obj == null) {
            deque.push(new DefaultTableProperties(name));
        } else {
            final DefaultTableProperties props = (DefaultTableProperties) deque.peek();
            deque.push(obj);
            deque.push(props.addColumnIfAbsent(name));
        }
    }

}
