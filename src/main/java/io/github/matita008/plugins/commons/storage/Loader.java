package io.github.matita008.plugins.commons.storage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to indicate the builder of a {@link Data} class.
 * <p>
 * The builder must be an implementation of {@link Data.Builder}
 *
 * If multiple fields/methods are marked {@code @Loader} there is no guarantee over which one is selected
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Loader {

}
