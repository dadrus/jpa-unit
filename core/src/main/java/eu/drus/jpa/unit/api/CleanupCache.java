package eu.drus.jpa.unit.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Determines when JPA second level cache cleanup should be triggered. Default test phase is
 * {@link CleanupPhase#AFTER}. If not specified the second level cache is not evicted.
 *
 * @see CleanupPhase
 */
@Target({
        TYPE, METHOD
})
@Retention(RUNTIME)
@Inherited
public @interface CleanupCache {

    /**
     * Phase when the second level cache cleanup should be triggered. Default phase is
     * {@link CleanupPhase#AFTER}.
     *
     * The behavior of the second level can be configured in the <code>persistence.xml</code>. If
     * not set to {@link CleanupPhase#NONE} the second level cache will be evicted regardless the
     * settings defined in the <code>persistence.xml</code>
     */
    CleanupPhase phase() default CleanupPhase.AFTER;
}
