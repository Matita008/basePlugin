package io.github.matita008.plugins.commons.storage;

import io.github.matita008.plugins.commons.Service;
import io.github.matita008.plugins.commons.logging.Log;

import java.io.Closeable;
import java.io.IOException;

public interface Storage extends Service {
   void closeAll();
   void save(Data data);
   Data get();
   
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
