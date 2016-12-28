package eu.drus.jpa.unit.annotation;

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
     * Whether the second level cache should be evicted. Default is true. The behavior of the second
     * level can be configured in the <code>persistence.xml</code>. If set to <code>true</code> the
     * setting here will clear the second level cache regardless the settings defined in the
     * <code>persistence.xml</code>
     */
    boolean value() default true;

    /**
     * Phase when the second level cache cleanup should be triggered. Default phase is
     * {@link CleanupPhase#AFTER}.
     */
    CleanupPhase phase() default CleanupPhase.AFTER;
}
