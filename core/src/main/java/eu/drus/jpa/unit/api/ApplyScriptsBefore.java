package eu.drus.jpa.unit.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Defines arbitrary SQL scripts which shall be executed after running the test method.
 */
@Target({
        TYPE, METHOD
})
@Retention(RUNTIME)
@Inherited
public @interface ApplyScriptsBefore {

    /**
     * SQL Scripts to execute
     */
    String[]value();
}
