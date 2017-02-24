package eu.drus.jpa.unit.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Verifies state of underlying data store using data sets defined by this annotation. Verification
 * is invoked after test's execution (including transaction if enabled).
 */
@Target({
        TYPE, METHOD
})
@Retention(RUNTIME)
@Inherited
public @interface ExpectedDataSets {

    /**
     * List of data set files used for comparison.
     */
    String[] value();

    /**
     * List of columns to be used for sorting rows to determine order of data sets comparison.
     */
    String[] orderBy() default {};

    /**
     * List of columns to be excluded.
     */
    String[] excludeColumns() default {};

    /**
     * Defines whether the performed verification about expected data sets is strict or not. In
     * strict mode all tables and entries not defined in the expected data sets are considered to be
     * an error.
     */
    boolean strict() default false;
}
