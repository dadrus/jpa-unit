package eu.drus.jpa.unit.cassandra.dataset;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;

import com.google.common.collect.Iterators;

public class FilteredDataSet implements DataSet {

    private DataSet dataSet;
    private Predicate<Table> filter;

    public FilteredDataSet(final DataSet dataSet, final Predicate<Table> filter) {
        this.dataSet = dataSet;
        this.filter = filter;
    }

    @Override
    public Iterator<Table> iterator() {
        return Iterators.filter(dataSet.iterator(), t -> filter.test(t));
    }

    @Override
    public Optional<Table> getTable(final String name) {
        final Optional<Table> table = dataSet.getTable(name);
        if (table.isPresent() && filter.test(table.get())) {
            return table;
        }
        return Optional.empty();
    }

}
