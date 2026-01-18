package io.github.matita008.plugins.commons.storage;

import lombok.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to indicate the loader method of a {@link Data} class.
 * <p>
 * The loader method must be static and return {@link Data} or a derivate
 * and must take a {@code Map<Value, Object>} as the only parameter, where the Object
 * is granted to be the type specified in the {@link Value} key
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Loader {
   /**
    *
    * @param name
    * @param type
    */
   @NonNull
   record Value(String name, Data.Type type){
   
   }
}
