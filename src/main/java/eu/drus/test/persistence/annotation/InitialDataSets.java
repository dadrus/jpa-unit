package eu.drus.test.persistence.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Provides information about data sets to be used for seeding test database before test method
 * execution and the corresponding seeding strategy. Default seeding strategy is
 * {@link DataSeedStrategy#INSERT}.
 *
 * @see DataSeedStrategy
 */
@Target({
        TYPE, METHOD
})
@Retention(RUNTIME)
@Inherited
public @interface InitialDataSets {

    /**
     * List of data set files used to seed the database.
     */
    String[] value();

    /**
     * Seeding strategy to be used while seeding the database. Default strategy is
     * {@link DataSeedStrategy#INSERT}.
     */
    DataSeedStrategy seedStrategy() default DataSeedStrategy.INSERT;
}
