package io.airlift.command;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a class as providing multiple command group metadata
 */
@Target(TYPE)
@Retention(RUNTIME)
@Inherited
@Documented
public @interface Groups
{
    Group[] value();
}
