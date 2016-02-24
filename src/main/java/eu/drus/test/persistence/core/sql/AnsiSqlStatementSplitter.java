package eu.drus.test.persistence.core.sql;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class AnsiSqlStatementSplitter {
    private static final String CHAR_SEQUENCE_PATTERN = "(?m)'([^']*)'|\"([^\"]*)\"";

    private static final String ANSI_SQL_COMMENTS_PATTERN = "--.*|//.*|(?s)/\\*.*?\\*/|(?s)\\{.*?\\}";

    private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

    private String statementDelimiter;

    public AnsiSqlStatementSplitter() {
        statementDelimiter = ";";
    }

    public AnsiSqlStatementSplitter(final String statementDelimiter) {
        this.statementDelimiter = statementDelimiter;
    }

    public void setStatementDelimiter(final String statementDelimiter) {
        this.statementDelimiter = statementDelimiter;
    }

    public String supports() {
        return "default";
    }

    public List<String> splitStatements(String script) {
        script = removeComments(escape(script));
        return splitStatements(new StringReader(script));
    }

    public List<String> splitStatements(final Reader reader) {
        final BufferedReader lineReader = new BufferedReader(reader);
        final List<String> statements = new ArrayList<String>();
        try {
            final StringBuilder readSqlStatement = new StringBuilder();
            String line;
            while ((line = lineReader.readLine()) != null) {
                final boolean isFullCommand = parseLine(line, readSqlStatement);
                if (isFullCommand) {
                    if (multipleInlineStatements(line)) {
                        statements.addAll(splitInlineStatements(line));
                    } else {
                        final String trimmed = unescape(readSqlStatement.toString().trim());
                        if (trimmed.length() > 0) {
                            statements.add(trimmed);
                        }
                    }
                    readSqlStatement.setLength(0);
                }
            }
            if (shouldExecuteRemainingStatements(readSqlStatement)) {
                statements.add(unescape(readSqlStatement.toString().trim()));
            }
        } catch (final Exception e) {
            throw new RuntimeException("Failed parsing file.", e);
        }

        return statements;
    }

    public String escape(final String source) {
        return source.replaceAll("(?m)&(.[a-zA-Z0-9]*);", "ape_special[$1]");
    }

    public String unescape(final String source) {
        return source.replaceAll("(?m)ape_special\\[(.[a-zA-Z0-9]*)]", "&$1;");
    }

    private String removeComments(final String script) {
        return script.replaceAll(ANSI_SQL_COMMENTS_PATTERN, "");
    }

    // -- Private methods

    private boolean parseLine(final String line, final StringBuilder sql) {
        final String trimmedLine = trimLine(line);
        sql.append(trimmedLine).append(LINE_SEPARATOR);

        return isFullCommand(trimmedLine);
    }

    private String trimLine(final String line) {
        return line.trim() + (isNewLineStatementDelimiter() ? LINE_SEPARATOR : "");
    }

    private boolean shouldExecuteRemainingStatements(final StringBuilder sql) {
        return sql.toString().trim().length() > 0;
    }

    private boolean isNewLineStatementDelimiter() {
        return "NEW_LINE".equals(statementDelimiter);
    }

    private List<String> splitInlineStatements(final String line) {
        final List<String> statements = new ArrayList<String>();
        final StringTokenizer sqlStatements = new StringTokenizer(line, statementDelimiter);
        while (sqlStatements.hasMoreElements()) {
            final String token = sqlStatements.nextToken();
            statements.add(unescape(token.trim()));
        }
        return statements;
    }

    private boolean multipleInlineStatements(final String line) {
        if (isNewLineStatementDelimiter()) {
            return false;
        }
        return new StringTokenizer(markCharSequences(line), statementDelimiter).countTokens() > 1;
    }

    private String markCharSequences(final String line) {
        return line.replaceAll(CHAR_SEQUENCE_PATTERN, "char_seq");
    }

    private boolean isFullCommand(final String line) {
        return lineEndsWithStatementDelimiter(line) || lineIsStatementDelimiter(line);
    }

    private boolean lineIsStatementDelimiter(final String line) {
        boolean isStatementDelimiter = line.equals(statementDelimiter);
        if (!isStatementDelimiter && isNewLineStatementDelimiter()) {
            isStatementDelimiter = line.matches("^\\r?\\n$|^\\r$");
        }
        return isStatementDelimiter;
    }

    private boolean lineEndsWithStatementDelimiter(final String line) {
        boolean ends = line.endsWith(statementDelimiter);
        if (!ends && isNewLineStatementDelimiter()) {
            ends = line.matches("^.+?\\r?\\n$|^.+?\\r$");
        }
        return ends;
    }
}
