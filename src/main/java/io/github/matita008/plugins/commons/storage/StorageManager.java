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

public class StorageManager {
   private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
   
   private static final Map<PluginBase, StorageManager> storageManagers = new ConcurrentHashMap<>();
   @Getter protected List<Class<? extends Data>> dataClasses = new ArrayList<>();
   @Getter protected Storage instance;
   
   protected StorageManager(PluginBase plugin) {
      instance = new HikariDatabase(plugin);
   }
   
   public void addDataClass(Class<? extends Data> dataClass){
      dataClasses.add(dataClass);
   }
   
   public void initDb(ConfigurationSection config){
      instance.init(config);
   }
   
   public static StorageManager getStorageManager(PluginBase plugin) {
      return storageManagers.get(plugin);
   }
   
   public static void registerDataClass(Class<? extends Data> dataClass){
      Plugin plugin = JavaPlugin.getProvidingPlugin(WALKER.getCallerClass());
      if(!(plugin instanceof PluginBase)) return;
      
      getStorageManager((PluginBase) plugin).dataClasses.add(dataClass);
   }
   
   public static void register(PluginBase plugin){
      registerManager(plugin, new StorageManager(plugin));
   }
   
   public static void registerManager(PluginBase plugin, StorageManager manager) {
      storageManagers.remove(plugin);
      storageManagers.put(plugin, manager);
      plugin.loadService(manager.getInstance());
   }
}
