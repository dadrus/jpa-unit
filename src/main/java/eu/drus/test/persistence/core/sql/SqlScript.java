package eu.drus.test.persistence.core.sql;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.StringTokenizer;

public class SqlScript implements Iterable<String> {

    private static String COMMENT_PATTERN = "(?:--.*)|(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:#.*)";

    private String script;

    public SqlScript(final String script) {
        this.script = script;
    }

    @Override
    public Iterator<String> iterator() {
        final StringTokenizer st = new StringTokenizer(script.replaceAll(COMMENT_PATTERN, ""), ";");

        return new Iterator<String>() {
            private String cached;
            private boolean hasNextCached;
            private boolean hasNext;

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
        };
    }

    @Override
    public Spliterator<String> spliterator() {
        throw new UnsupportedOperationException();
    }

}
