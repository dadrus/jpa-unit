package eu.drus.jpa.unit.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Determines when database cleanup should be triggered. Default test phase is
 * {@link CleanupPhase#AFTER}. If not specified otherwise the whole database is erased. You can
 * change this behavior by setting up {@link #strategy()} field.
 *
 * @see CleanupPhase
 * @see CleanupStrategy
 */
@Target({
        TYPE, METHOD
})
@Retention(RUNTIME)
@Inherited
public @interface Cleanup {

    /**
     * Phase when the database cleanup should be triggered. Default phase is
     * {@link CleanupPhase#AFTER}.
     */
    CleanupPhase phase() default CleanupPhase.AFTER;

    /**
     * Strategy to apply while erasing the database. Default strategy is
     * {@link CleanupStrategy#STRICT}.
     */
    CleanupStrategy strategy() default CleanupStrategy.STRICT;
}
