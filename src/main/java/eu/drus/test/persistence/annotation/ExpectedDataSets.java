package eu.drus.test.persistence.annotation;

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
    String[]value();

    /**
     * List of columns to be used for sorting rows to determine order of data sets comparison.
     */
    String[]orderBy() default "";

    /**
     * List of columns to be excluded.
     */
    String[]excludeColumns() default "";
}
