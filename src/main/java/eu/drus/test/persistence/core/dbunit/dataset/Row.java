package eu.drus.test.persistence.core.dbunit.dataset;

import java.util.HashMap;
import java.util.Map;

public class Row {
    private final Map<String, String> cells = new HashMap<>();

    public Row(final Map<String, String> cells) {
        for (final Map.Entry<String, String> cell : cells.entrySet()) {
            this.cells.put(String.valueOf(cell.getKey()), String.valueOf(cell.getValue()));
        }
    }

    public String valueOf(final String name) {
        return cells.get(name);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Row)) {
            return false;
        }

        final Row other = (Row) obj;

        final Map<String, String> otherCells = other.cells;

        if (cells.size() != otherCells.size()) {
            return false;
        }

        for (final Map.Entry<String, String> cell : cells.entrySet()) {
            final String name = cell.getKey();
            final String value = cell.getValue();
            if (!value.equals(otherCells.get(name))) {
                return false;
            }
        }

        return true;

    }

    @Override
    public int hashCode() {
        final int prime = 17;
        int result = 1;
        result = prime * result + (cells == null ? 0 : cellHashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Row [cells=" + toString(cells) + "]";
    }

    private String toString(final Map<String, String> cells2) {
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<String, String> cell : cells.entrySet()) {
            sb.append("{key = ").append(cell.getKey()).append(", value = ").append(cell.getValue()).append("} ");
        }
        return sb.toString();
    }

    private int cellHashCode() {
        final int prime = 41;
        int result = 1;
        for (final Map.Entry<String, String> cell : cells.entrySet()) {
            result = prime * result + cell.getKey().hashCode();
            result = prime * result + cell.getValue().hashCode();
        }
        return result;
    }
}
