package io.github.matita008.plugins.commons.commands;

import io.github.matita008.plugins.commons.commands.internal.NullCommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Command {
   Class<? extends ICommand> parent() default NullCommand.class;
   String name();
   String description() default "";
   String usage() default "";
   String[] aliases() default { };
   String[] permission() default { };
   boolean consoleAllowed() default false;
   
   @Retention(RetentionPolicy.RUNTIME)
   @Target(ElementType.METHOD)
   @interface Execute {}
}
