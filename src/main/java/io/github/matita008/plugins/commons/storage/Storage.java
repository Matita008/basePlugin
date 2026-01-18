package io.github.matita008.plugins.commons.storage;

import io.github.matita008.plugins.commons.Service;
import io.github.matita008.plugins.commons.logging.Log;
import org.bukkit.configuration.ConfigurationSection;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public interface Storage extends Service {//TODO: add yml/similar support
   void init(ConfigurationSection config);
   void closeAll();
   void save(Data data);
   Data get(Class<? extends Data> clazz, List<Data.Value> keys);
   
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
