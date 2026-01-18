package io.github.matita008.plugins.commons;

import io.github.matita008.plugins.commons.logging.Log;
import io.github.matita008.plugins.commons.storage.Data;
import io.github.matita008.plugins.commons.storage.Storage;
import io.github.matita008.plugins.commons.storage.StorageManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public abstract class PluginBase extends JavaPlugin {
   private final List<Service> services = new ArrayList<>();
   
   @Override
   public void onLoad() {
      if(getResource("config.yml") != null && getConfig().contains("storage") && getConfig().isConfigurationSection("storage")) {
         StorageManager.register(this);
      }
   }
   
   @Override
   public void onDisable() {
      //Ensure storages are the last to be disabled, so any other service can save data
      services.stream().filter(s -> !(s instanceof Storage)).forEach(Service::unload);
      services.stream().filter(s -> s instanceof Storage).forEach(Service::unload);
      services.clear();
   }
   
   public void loadService(Service service){
      if(service == null) {
         Log.log(Level.WARNING, "Please report this to " + String.join(", ", getDescription().getAuthors()) + " (error: Service is null)", new NullPointerException());
         return;
      }
      service.load();
      services.add(service);
   }
   
   public void registerDataClass(Class<? extends Data> dataClass){
      StorageManager.getStorageManager(this).addDataClass(dataClass);
   }
   
   protected void enableDatabase() {
      enableDatabase(getConfig().getConfigurationSection("storage"));
   }
   
   protected void enableDatabase(ConfigurationSection section) {
      StorageManager.getStorageManager(this).initDb(section);
   }
}
