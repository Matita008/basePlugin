package com.github.matita008.plugins.commons.logging;

import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Log {
   private static final Map<Plugin, Log> logs = new ConcurrentHashMap<>();
   private String prefix;
   
   public static Log getLog(Plugin plugin) {
      return Optional.ofNullable(logs.get(plugin)).orElseGet(() -> {
         Log log = new Log(plugin.getDescription().getPrefix());
         logs.put(plugin, log);
         return log;
      });
   }
   
   public static Log getLog(Class<?> clazz) {
      try {
         return getLog(JavaPlugin.getProvidingPlugin(clazz));
      } catch (Exception e) {
         return new Log(clazz.getName());
      }
   }
   
   public static Log getLog() {
      return getLog(StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass());
   }
   
   public void log(Level level, String message, Throwable throwable) {
      Bukkit.getLogger().log(level, "[" + prefix + "]" + message, throwable);
   }
   
   public void warn(String message) {
      Bukkit.getLogger().log(Level.WARNING, "[" + prefix + "]" + message);
   }
   
   public void severe(String message) {
      Bukkit.getLogger().log(Level.SEVERE, "[" + prefix + "]" + message);
   }
}
