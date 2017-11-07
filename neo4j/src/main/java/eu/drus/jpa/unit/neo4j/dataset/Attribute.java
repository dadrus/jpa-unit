package eu.drus.jpa.unit.neo4j.dataset;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Attribute {

    private String name;

    private Object value;

    private boolean isId;

    public Attribute(final String name, final Object value, final boolean isId) {
        this.name = name;
        this.value = value;
        this.isId = isId;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public boolean isId() {
        return isId;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(name).append("=").append(value);
        if (isId) {
            builder.append("(").append("id").append(")");
        }
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(name);
        builder.append(value);
        builder.append(isId);
        return builder.toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof Attribute) {
            final Attribute other = (Attribute) obj;
            final EqualsBuilder builder = new EqualsBuilder();
            builder.append(name, other.name);
            builder.append(value, other.value);
            builder.append(isId, other.isId);
            return builder.isEquals();
        }

        return false;
    }
}
