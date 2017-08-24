package eu.drus.jpa.unit.cassandra.dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PrimaryKey {

    private List<Column> partitionKeys = new ArrayList<>();
    private List<Column> clusteringKeys = new ArrayList<>();

    private PrimaryKey() {}

    public static Builder builder() {
        return new Builder(new PrimaryKey());
    }

    public List<Column> getPartitionKeys() {
        return Collections.unmodifiableList(partitionKeys);
    }

    public List<Column> getClusteringKeys() {
        return Collections.unmodifiableList(clusteringKeys);
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(partitionKeys).append(clusteringKeys);
        return builder.toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof PrimaryKey) {
            final PrimaryKey other = (PrimaryKey) obj;
            final EqualsBuilder builder = new EqualsBuilder();
            builder.append(partitionKeys, other.partitionKeys);
            builder.append(clusteringKeys, other.clusteringKeys);
            return builder.isEquals();
        }

        return false;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(getClass().getSimpleName());
        builder.append(" (");
        if (partitionKeys.size() > 1) {
            builder.append("(");
        }
        buildNameList(builder, partitionKeys);
        if (partitionKeys.size() > 1) {
            builder.append(")");
        }
        if (!clusteringKeys.isEmpty()) {
            builder.append(", ");
            buildNameList(builder, clusteringKeys);
        }
        builder.append(")");
        return builder.toString();
    }

    private void buildNameList(final StringBuilder builder, final List<Column> list) {
        final Iterator<Column> it = list.iterator();
        while (it.hasNext()) {
            builder.append(it.next().getName());
            if (it.hasNext()) {
                builder.append(", ");
            }
        }
    }

    public static class Builder {

        private PrimaryKey primaryKey;

        public Builder(final PrimaryKey primaryKey) {
            this.primaryKey = primaryKey;
        }

        public Builder withPartitionKey(final Column column) {
            primaryKey.partitionKeys.add(column);
            return this;
        }

        public Builder withClusteringKey(final Column column) {
            primaryKey.clusteringKeys.add(column);
            return this;
        }

        public PrimaryKey build() {
            return primaryKey;
        }
    }
}
