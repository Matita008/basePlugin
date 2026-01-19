package io.github.matita008.plugins.commons.storage;

import io.github.matita008.plugins.commons.Service;
import io.github.matita008.plugins.commons.logging.Log;
import org.bukkit.configuration.ConfigurationSection;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * The {@code Storage} interface extends the {@link Service} interface and represents
 * a storage service for managing data. Implementations of this interface
 * provide functionalities to initialize, save, retrieve, and close resources
 * associated with the storage system.
 */
public interface Storage extends Service {//TODO: add yml/similar support
   /**
    * Initializes the storage system using the provided configuration section.
    * This method is responsible for setting up required resources and configurations
    * for the storage service.
    *
    * @param config the {@link ConfigurationSection} containing the configuration information
    */
   void init(ConfigurationSection config);
   /**
    * Closes all resources or connections used by the implementing class.
    */
   void closeAll();
   /**
    * Saves the specified {@link Data} instance to the storage.
    *
    * @param data the {@link Data} object to be saved to the storage
    */
   void save(Data data);
   /**
    * Retrieves a {@link Data} object of the specified type corresponding to the given keys.
    *
    * @param clazz the class type of the {@link Data} object to retrieve
    * @param keys a list of {@link Data.Value} instances representing the keys used to locate the desired data
    * @return a {@link Data} object matching the given class type and keys, or {@code null} if no matching data is found
    */
   Data get(Class<? extends Data> clazz, List<Data.Value> keys);
   
   /**
    * Closes the provided {@code Closeable} resources, suppressing any {@link IOException}
    * that occurs during their closure. If an exception is encountered, logs the error
    * indicating the resource that failed to close.
    *
    * @param closeable Varargs parameter representing one or more {@link Closeable}
    *                  resources to be closed. Pass multiple resources as arguments,
    *                  and the method will attempt to close each in turn.
    */
   default void close(Closeable... closeable) {
      for (Closeable c : closeable) {
         try {
            c.close();
         } catch (IOException e) {
            Log.getLog("Storage").doSevere("An error occurred while closing " + c, e);
         }
      }
   }
   
   @Override
   default void unload() {
      closeAll();
   }
}
