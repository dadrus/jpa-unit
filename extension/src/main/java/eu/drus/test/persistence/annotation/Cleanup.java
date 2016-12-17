package eu.drus.test.persistence.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Determines when database cleanup should be triggered. Default test phase is
 * {@link CleanupPhase#AFTER}. If not specified otherwise the whole database is erased. You can
 * change this behavior by setting up {@link #strategy()} field. In addition you can define whether
 * the second level cache should be evicted.
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

    /**
     * Whether the second level cache should be evicted. Default is false. The behavior of the
     * second level can be configured in the <code>persistence.xml</code>. If set to
     * <code>true</code> the setting here will clear the second level cache regardless the settings
     * defined in the <code>persistence.xml</code>
     */
    boolean evictCache() default false;
}
