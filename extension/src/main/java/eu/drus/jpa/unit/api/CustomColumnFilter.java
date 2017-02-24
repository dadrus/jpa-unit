package eu.drus.jpa.unit.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.dbunit.dataset.filter.IColumnFilter;

/**
 * Provides ability to define custom column filters ({@link org.dbunit.dataset.filter.IColumnFilter}
 * ) used when comparing datasets specified by {@link ExpectedDataSets} annotation. <br>
 * <br>
 * The use of IColumnFilter implementations is described
 * <a href="http://dbunit.sourceforge.net/faq.html#columnfilter">here</a>. <br>
 */
@Target({
        TYPE, METHOD
})
@Retention(RUNTIME)
@Inherited
public @interface CustomColumnFilter {
    /**
     * Custom column filters to be applied in the specified order. Each concrete implementation is
     * expected to have default non-argument constructor which will be used when creating an
     * instance of the filter.
     */
    Class<? extends IColumnFilter>[] value();
}
