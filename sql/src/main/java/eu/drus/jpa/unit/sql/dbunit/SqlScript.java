package eu.drus.jpa.unit.sql.dbunit;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.StringTokenizer;

public class SqlScript implements Iterable<String> {

    private static final String COMMENT_PATTERN = "(?:--.*)|(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:#.*)";

    private static class SqlScriptIterator implements Iterator<String> {

        private final StringTokenizer st;
        private String cached;
        private boolean hasNextCached;
        private boolean hasNext;

        private SqlScriptIterator(final String script) {
            st = new StringTokenizer(script.replaceAll(COMMENT_PATTERN, ""), ";");
        }

        @Override
        public boolean hasNext() {
            return hasNextCached ? hasNext : findNextMatch();
        }

        @Override
        public String next() {
            if (hasNext()) {
                hasNextCached = false;
                return cached;
            } else {
                throw new NoSuchElementException();
            }
        }

        private boolean findNextMatch() {
            boolean match = false;
            while (!match && st.hasMoreElements()) {
                cached = st.nextToken().trim();
                match = !cached.isEmpty();
            }
            hasNextCached = true;
            hasNext = match;
            return match;
        }

    }

    private String script;

    public SqlScript(final String script) {
        this.script = script;
    }

    @Override
    public Iterator<String> iterator() {
        return new SqlScriptIterator(script);
    }

    @Override
    public Spliterator<String> spliterator() {
        throw new UnsupportedOperationException();
    }

}
