package io.airlift.command;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a class as providing command group metadata
 */
@Target(TYPE)
@Retention(RUNTIME)
@Inherited
@Documented
public @interface Group
{
    public static final class DEFAULT {}
    
    /**
     * Name of the group.
     */
    String name();

    /**
     * Description of the group.
     */
    String description() default "";

    /**
     * Default command class for the group (optional)
     */
    Class<?> defaultCommand() default DEFAULT.class;

    /**
     * command classes to add to the group (optional)
     */
    Class<?>[] commands() default {};
}
