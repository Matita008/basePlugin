package io.github.matita008.plugins.commons.logging;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class Log {
   private static final Map<Plugin, Log> logs = new ConcurrentHashMap<>();
   private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
   private final String prefix;
   
   private Log(String prefix) {
      this.prefix = "[" + prefix + "]";
   }
   
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
   
   public static Log getLog(String prefix) { return new Log(prefix); }
   
   public static Log getLog() {
      return getLog(WALKER.getCallerClass());
   }
   
   public static void warn(String message) { getLog(WALKER.getCallerClass()).doWarn(message); }
   public static void warn(String message, Throwable throwable) { getLog(WALKER.getCallerClass()).doWarn(message, throwable); }
   public static void severe(String message) { getLog(WALKER.getCallerClass()).doSevere(message); }
   public static void severe(String message, Throwable throwable) { getLog(WALKER.getCallerClass()).doSevere(message, throwable); }
   public static void log(Level level, String message, Throwable  throwable) { getLog(WALKER.getCallerClass()).doLog(level, message, throwable); }
   public static void log(Level level, String message) { getLog(WALKER.getCallerClass()).doLog(level, message); }
   
   public void doWarn(String message, Throwable throwable) { doLog(Level.WARNING, message, throwable); }
   public void doWarn(String message) { doLog(Level.WARNING, message); }
   public void doSevere(String message, Throwable throwable) { doLog(Level.SEVERE, message, throwable); }
   public void doSevere(String message) { doLog(Level.SEVERE, message); }
   
   public void doLog(Level level, String message) {
      Bukkit.getLogger().log(level, prefix + message);
   }
   
   public void doLog(Level level, String message, Throwable throwable) {
      Bukkit.getLogger().log(level, prefix + message, throwable);
   }
   
   public Log addPrefix(String prefix) {
      return new Log(this.prefix + " [" + prefix + "]");
   }
}
