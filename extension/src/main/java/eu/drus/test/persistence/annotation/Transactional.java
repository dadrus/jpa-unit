package eu.drus.test.persistence.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that test will be wrapped in the transaction. <br>
 * It can be defined either on a class or a method level, where latter takes precedence if used. If
 * not defined at all the {@link TransactionMode#COMMIT} mode is aplied.
 *
 * @see TransactionMode
 */
@Target({
        TYPE, METHOD
})
@Retention(RUNTIME)
@Inherited
public @interface Transactional {

    /**
     * Mode of transaction. {@link TransactionMode#COMMIT} is the default mode.
     */
    TransactionMode value() default TransactionMode.COMMIT;
}
