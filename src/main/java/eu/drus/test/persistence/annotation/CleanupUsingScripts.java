package eu.drus.test.persistence.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Defines SQL scripts used for cleaning test database.
 *
 * @see {@link CleanupPhase}
 */
@Target({
        TYPE, METHOD
})
@Retention(RUNTIME)
@Inherited
public @interface CleanupUsingScripts {

    /**
     * SQL Scripts to apply
     */
    String[]value();

    /**
     * Phase when the above scripts shall be executed. Default phase is {@link CleanupPhase#AFTER}.
     */
    CleanupPhase phase() default CleanupPhase.AFTER;
}
