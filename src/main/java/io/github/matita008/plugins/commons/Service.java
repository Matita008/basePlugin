package io.github.matita008.plugins.commons;

/**
 * Represents a generic service that can be loaded or unloaded. This interface is
 * intended to be implemented by classes that define specific services which need
 * to be managed during a plugin's lifecycle.
 *
 * A service typically provides functionality that can be initialized with the
 * {@link #load()} method and cleaned up with the {@link #unload()} method. The default
 * implementation of these methods does nothing, allowing implementations to override
 * as needed.
 */
public interface Service {
   /**
    * Initializes the service. This method provides a default implementation that does nothing.
    * It can be overridden by implementing classes to include specific initialization logic.
    *
    * The {@code load} method is invoked when the service is being registered or
    * initialized, ensuring that all necessary setup for the
    * service has been completed.
    */
   default void load() { }
   /**
    * Unloads the service, performing any necessary cleanup or shutdown
    * procedures for the service implementation. This method is intended
    * to be called when the service is no longer needed or is being
    * removed from active use.
    *
    * By default, this method does nothing and should be overridden by
    * implementing classes to define specific unload behavior if required.
    */
   default void unload() { }
}
