package com.github.jlgrock.poormansioc;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Qualifier that can match the qualifier defined at read time.  This is helpful in configuration file loading, where
 * you would like to specify a bean of the same type with a different bean name.
 */
@Target(PARAMETER)
@Retention(RUNTIME)
@Documented
public @interface Qualifier {
    /**
     * The name of the
     */
    String value() default "";
}
