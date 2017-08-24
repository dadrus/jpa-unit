package eu.drus.jpa.unit.cassandra.dataset;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Column {

    private ColumnType type;
    private String name;

    public Column(final String name) {
        this(name, ColumnType.UNKNOWN);
    }

    public Column(final String name, final ColumnType type) {
        this.type = type;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ColumnType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(type).append(name);
        return builder.toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Column) {
            final Column other = (Column) obj;
            final EqualsBuilder builder = new EqualsBuilder();
            builder.append(type, other.type);
            builder.append(name, other.name);
            return builder.isEquals();
        }

        return false;
    }

    @Override
    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("name", name);
        builder.append("type", type);
        return builder.build();
    }
}
