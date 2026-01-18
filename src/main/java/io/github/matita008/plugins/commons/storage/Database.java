package io.github.matita008.plugins.commons.storage;

import io.github.matita008.plugins.commons.logging.Log;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;

public abstract class Database implements Storage{
   protected abstract Connection getConnection();
   
   @Override
   public void close(Closeable... closeable) {
      for (Closeable c : closeable) {
         try {
            c.close();
         } catch (IOException e) {
            Log.getLog("DB").doSevere("An error occurred while closing " + c, e);
         }
      }
   }
}
