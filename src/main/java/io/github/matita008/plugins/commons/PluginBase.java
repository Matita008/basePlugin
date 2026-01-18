package io.github.matita008.plugins.commons;

import io.github.matita008.plugins.commons.logging.Log;
import io.github.matita008.plugins.commons.storage.Storage;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public abstract class PluginBase extends JavaPlugin {
   @Override
   public void onDisable() {
      //Ensure storages are the last to be disabled, so any other service can save data
      services.stream().filter(s -> !(s instanceof Storage)).forEach(Service::unload);
      services.stream().filter(s -> s instanceof Storage).forEach(Service::unload);
      services.clear();
   }
   
   private final List<Service> services = new ArrayList<>();
   public void loadService(Service service){
      if(service == null) {
         Log.log(Level.WARNING, "Please report this to the devs (error: Service is null)", new NullPointerException());
         return;
      }
      service.load();
      services.add(service);
   }
}
