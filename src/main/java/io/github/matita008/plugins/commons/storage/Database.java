package io.github.matita008.plugins.commons.storage;

import io.github.matita008.plugins.commons.PluginBase;
import io.github.matita008.plugins.commons.logging.Log;
import lombok.*;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.*;
import java.sql.Connection;

@AllArgsConstructor
public abstract class Database implements Storage{
   protected abstract Connection getConnection();
   @Getter protected final PluginBase plugin;
   
   @Override
   public void close(Closeable... closeable) {
      for (Closeable c : closeable) {
         try {
            c.close();
         } catch (IOException e) {
            Log.getLog(plugin).addPrefix("DB").doSevere("An error occurred while closing " + c, e);
         }
      }
   }
   
   protected Data.Builder getBuilder(Class<? extends Data> clazz) {
      Method[] methods = clazz.getDeclaredMethods();
      
      for (Method m : methods) {
         if(!(m.isAnnotationPresent(Loader.class) && Modifier.isStatic(m.getModifiers()))) continue;
         if(!(m.getParameterCount() == 0 && Data.Builder.class.isAssignableFrom(m.getReturnType()))) continue;
         
         try {
            m.setAccessible(true);
            return (Data.Builder) m.invoke(null);
         } catch (InaccessibleObjectException | InvocationTargetException | IllegalAccessException ignored) { }
      }
      
      Field[] fields = clazz.getFields();
      
      for(Field field : fields){
         if(!(field.isAnnotationPresent(Loader.class) && Modifier.isStatic(field.getModifiers()))) continue;
         if(!Data.Builder.class.isAssignableFrom(field.getDeclaringClass())) continue;
         
         try {
            field.setAccessible(true);
            return (Data.Builder) field.get(null);
         } catch (InaccessibleObjectException | SecurityException | IllegalAccessException ignored) { }
      }
      return null;
   }
}
