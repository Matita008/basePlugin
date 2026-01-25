package io.github.matita008.plugins.commons;

import io.github.matita008.plugins.base.ApiPlugin;
import io.github.matita008.plugins.commons.commands.CommandService;
import io.github.matita008.plugins.commons.logging.Log;
import io.github.matita008.plugins.commons.storage.Data;
import io.github.matita008.plugins.commons.storage.Storage;
import io.github.matita008.plugins.commons.storage.StorageManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;

public abstract class PluginBase extends JavaPlugin {
   private final List<Service> services = new ArrayList<>();
   private static int count = 0;
   
   @Override
   public void onLoad() {
      count++;
      if(getResource("config.yml") != null && getConfig().contains("storage") && getConfig().isConfigurationSection("storage")) {
         StorageManager.register(this);
      }
      
      if(getSettings().metricsId() > 1) Metric.init(this);
   }
   
   /**
    * Called when this plugin is enabled
    * <p>
    * please call super.onEnable() at the end to ensure relevant services are loaded
    */
   @Override
   public void onEnable() {
      CommandService.get(this).registerCommands();
      
      services.forEach(service -> {
         try {
            service.load();
         } catch (Throwable t) {
            Log.log(Level.WARNING, "Failed to load service " + service.getClass(), t);
            services.remove(service);
         }
      });
   }
   
   /**
    * Called when this plugin is disabled
    * <p>
    * please call super.onDisable() at the end to ensure all services are disabled
    */
   @Override
   public void onDisable() {
      //Ensure storages are the last to be disabled, so any other service can save data
      services.stream().filter(s -> !(s instanceof Storage)).forEach(Service::unload);
      services.stream().filter(s -> s instanceof Storage).forEach(Service::unload);
      services.clear();
   }
   
   /**
    * Loads the specified service into the current plugin instance.
    * If the service is null, a warning will be logged, and the method will return without proceeding further.
    *
    * @param service the service that needs to be loaded; must not be null
    */
   public void loadService(Service service){
      if(service == null) {
         Log.log(Level.WARNING, "Please report this to " + String.join(", ", getDescription().getAuthors()) + " (error: Service is null)", new NullPointerException());
         return;
      }
      services.add(service);
   }
   
   /**
    * Registers a new data class to be managed by the storage system of the current plugin.
    * This method allows the plugin to define custom data types for use with its storage system.
    *
    * @param dataClass the class to be registered as a data type; must extend the {@link Data} interface
    */
   public void registerDataClass(Class<? extends Data> dataClass){
      StorageManager.getStorageManager(this).addDataClass(dataClass);
   }
   
   /**
    * This function should be called only once to start the database
    */
   protected void enableDatabase() {
      enableDatabase(getConfig().getConfigurationSection("storage"));
   }
   
   /**
    * This function should be called only once to start the database
    * @param section The configuration to use to load the database
    */
   protected void enableDatabase(ConfigurationSection section) {
      StorageManager.getStorageManager(this).initDb(section);
   }
   
   /**
    * The settings to use to configure the {@link Metric bStat Metrics}
    * <p>
    * Each plugin that wants to have statistic shall implement this method
    * @return A {@link Settings} object for this instance
    */
   public @NotNull Settings getSettings() { return new Settings(); }
   
   /**
    * A class representing the configuration to use to load the metrics instance for bStats
    * @param metricsId the metric iId, or -1 to disable
    * @param charts A list of additional charts to track
    */
   public record Settings(int metricsId, List<Chart> charts){
      /** Create a new instance that will *not* track data */
      public Settings() { this(-1); }
      
      /**
       * Create a new instance to track servers using this plugin
       * @param metricsId the metric id, or -1 to disable
       */
      public Settings(int metricsId) {
         this(metricsId, new ArrayList<>());
         if(StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass().equals(ApiPlugin.class))
            charts.add(new Chart(ChartType.SINGLE_LINE, "plugins_count", ()->count));
      }
      
      public enum ChartType {
         ADVANCED_BAR, ADVANCED_PIE, DRILLDOWN_PIE, MULTI_LINE, SIMPLE_BAR, SIMPLE_PIE, SINGLE_LINE,
      }
      
      /**
       * Represent an additional chart
       * @param type The type of the chart
       * @param id the id of the chart
       * @param valueSupplier a supplier that returns the value of this chart; this will be called every update.
       * Please make sure the type is correct, as there are no compile-type checks
       */
      public record Chart(ChartType type, String id, Callable<?> valueSupplier) { }
   }
}