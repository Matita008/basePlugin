package io.github.matita008.plugins.commons.storage;

import io.github.matita008.plugins.commons.PluginBase;
import io.github.matita008.plugins.commons.storage.implementations.HikariDatabase;
import lombok.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The StorageManager class is responsible for managing storage-related operations for plugins.
 * It provides functionality to initialize a storage instance, register data classes,
 * and manage storage configurations. Instances of this class are associated with specific plugins,
 * and plugins must register a StorageManager instance to utilize its functionality.
 */
public class StorageManager {
   private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
   
   private static final Map<PluginBase, StorageManager> storageManagers = new ConcurrentHashMap<>();
   @Getter protected List<Class<? extends Data>> dataClasses = new ArrayList<>();
   @Getter protected Storage instance;
   
   protected StorageManager(PluginBase plugin) {
      instance = new HikariDatabase(plugin);
   }
   
   /**
    * Adds a data class to the storage manager.
    * This method allows registering a class that extends the {@code Data} type,
    * enabling its usage within the storage system.
    *
    * @param dataClass the class of type {@code Data} to be added
    */
   public void addDataClass(Class<? extends Data> dataClass){
      dataClasses.add(dataClass);
   }
   
   /**
    * Initializes the database with the given configuration.
    * This method delegates the initialization process to the underlying storage instance.
    *
    * @param config the configuration section containing necessary configuration settings
    */
   public void initDb(ConfigurationSection config){
      instance.init(config);
   }
   
   /**
    * Retrieves the {@code StorageManager} instance associated with the given plugin.
    * This method allows plugins to access their specific {@code StorageManager}, which handles
    * storage-related operations such as managing data classes and configuring storage instances.
    *
    * @param plugin the plugin for which the {@code StorageManager} is to be retrieved
    * @return the {@code StorageManager} associated with the given plugin, or {@code null} if no {@code StorageManager}
    *         is registered for the plugin
    */
   public static StorageManager getStorageManager(PluginBase plugin) {
      return storageManagers.get(plugin);
   }
   
   /**
    * Registers a data class with the {@code StorageManager} associated with the current plugin.
    * This method allows adding a class that extends the {@code Data} type to the list of managed data classes
    * for the plugin's {@code StorageManager}, enabling integration with the storage system.
    *
    * @param dataClass the class of type {@code Data} to be registered
    */
   public static void registerDataClass(Class<? extends Data> dataClass){
      Plugin plugin = JavaPlugin.getProvidingPlugin(WALKER.getCallerClass());
      if(!(plugin instanceof PluginBase)) return;
      
      getStorageManager((PluginBase) plugin).dataClasses.add(dataClass);
   }
   
   /**
    * Registers a {@code StorageManager} for the given plugin.
    * This method initializes a new {@code StorageManager} instance associated with the specified plugin
    * and delegates the registration to the {@code registerManager} method.
    *
    * @param plugin the plugin for which the {@code StorageManager} is to be registered; must not be null
    */
   public static void register(PluginBase plugin){
      registerManager(plugin, new StorageManager(plugin));
   }
   
   /**
    * Associates a specific {@code StorageManager} with a given plugin.
    * This method registers the provided {@code StorageManager} instance for the plugin, replacing any
    * previously registered manager for the same plugin. Additionally, the storage manager's instance is
    * loaded as a service for the plugin.
    *
    * @param plugin  the plugin for which the {@code StorageManager} is to be registered; must not be null
    * @param manager the {@code StorageManager} instance to be associated with the plugin; must not be null
    */
   public static void registerManager(PluginBase plugin, StorageManager manager) {
      storageManagers.remove(plugin);
      storageManagers.put(plugin, manager);
      plugin.loadService(manager.getInstance());
   }
}
