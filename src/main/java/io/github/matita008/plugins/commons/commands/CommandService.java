package io.github.matita008.plugins.commons.commands;

import io.github.matita008.plugins.commons.PluginBase;
import io.github.matita008.plugins.commons.Service;
import io.github.matita008.plugins.commons.commands.internal.NullCommand;
import io.github.matita008.plugins.commons.logging.Log;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Class used to crate and manage {@link org.bukkit.command.Command} and to load them
 * <p>
 * This class contains utility methods ({@link #get()},{@link #get(PluginBase)}) to get the instance
 * associated with each plugin
 * <p>
 * This class shall be instantiated once per {@link PluginBase plugin}, and subclasses shall add implementations
 * to the {@link #commandServices map} containing each plugin-instance pair
 *
 * @see Command
 * @see ICommand
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CommandService implements Service {
   private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
   protected static final Map<PluginBase, CommandService> commandServices = new ConcurrentHashMap<>();
   @Getter private final PluginBase plugin;
   protected List<ICommand> commands = new ArrayList<>();
   
   /**
    * Returns a cached instance or creates a new one
    * @param plugin the non-null plugin whose instance is needed
    * @return the instance associated with the plugin
    */
   public static CommandService get(@NotNull PluginBase plugin) {
      if(!commandServices.containsKey(plugin)) commandServices.put(plugin, new CommandService(plugin));
      return commandServices.get(plugin);
   }
   
   /**
    * Retrieves a {@link CommandService} instance associated with the plugin that provided the caller class.
    * If the plugin associated with the caller class is not an instance of {@link PluginBase}, this method returns null.
    *
    * @return the {@code CommandService} instance for the calling plugin or null if the plugin is not an instance of {@code PluginBase}.
    */
   public static CommandService get() {
      Plugin plugin = JavaPlugin.getProvidingPlugin(WALKER.getCallerClass());
      if(!(plugin instanceof PluginBase)) return null;
      return get((PluginBase) plugin);
   }
   
   /**
    * Register a new {@link ICommand} object to this instance
    * @param command The command to add
    */
   public void registerCommand(ICommand command) {
      commands.add(command);
   }
   
   /**
    * Register a new command to this instance
    * <p>
    * The object class must be annotated with {@link Command}
    * @param command The command to add
    * @throws RuntimeException if the object class is not annotated with {@code Command}
    */
   public void registerCommand(GeneratedCommand command) {
      if(!command.getClass().isAnnotationPresent(Command.class)) throw new RuntimeException(command.getClass().getName() + " is not annotated with @Command");
      
      Command annotation = command.getClass().getAnnotation(Command.class);
      
      String usage = annotation.usage().isBlank() ?  "/" + annotation.name() : annotation.usage();
      commands.add(new CustomCommand(annotation.name(), annotation.description(), usage, List.of(annotation.aliases()),
          command::run, List.of(annotation.permission()), annotation.consoleAllowed()));
   }
   
   /**
    * Registers a new command based on the given class. The class must be annotated with {@link Command}
    * and must have a method annotated with {@link Command.Execute}; if multiples are found there is no guarantee over which is selected.
    * <p>
    * The method annotated with {@code Command.Execute} must have at most 2 parameters, of type ({@link CommandSender} or {@link Player}) and {@code String[]}
    *
    * @param commandClass The class annotate with {@code Command} to initialize
    *
    * @throws RuntimeException If the class does not have {@link Command the required annotation}
    */
   public void registerCommand(Class<?> commandClass) {
      if(!commandClass.isAnnotationPresent(Command.class)) throw new RuntimeException(commandClass.getName() + " is not annotated with @Command");
      
      Command annotation = commandClass.getAnnotation(Command.class);
      
      Log log = Log.getLog(commandClass);
      BiFunction<CommandSender, String[], Boolean> command = null;
      boolean allowConsole = false;
      Object instance = null;
      boolean skip = false;
      
      //SO thank you: https://stackoverflow.com/a/6593661
      for (final Method method : commandClass.getDeclaredMethods()) {
         if(!method.isAnnotationPresent(Command.Execute.class)) continue;
         method.setAccessible(true);
         
         if(!Modifier.isStatic(method.getModifiers()) && (instance == null || skip)) {
            try {
               Constructor<?> c = commandClass.getConstructor(Void.class);
               c.setAccessible(true);
               instance = c.newInstance((Object) null);
            } catch (ReflectiveOperationException e) {
               skip = true;
               log.doSevere(commandClass.getName() + "doesn't have a no-args constructor", e);
               continue;
            }
         }
         
         Class<?> ret = method.getReturnType();
         Predicate<Object> getRet;
         if(ret == Boolean.class) getRet = o -> (boolean) o;
         else if(ret == Void.class) getRet = o -> true;
         else getRet = o -> o != null;
         
         Parameter[] parameters = method.getParameters();
         if(parameters.length > 2) {
            log.doSevere("Class " + commandClass.getName() + " can have at most 2 parameters");
            continue;
         }
         
         Object finstance = instance;
         
         switch(parameters.length) {
            case 0 -> {
               allowConsole = true;
               command = (sender, args) -> {
                  try {
                     return getRet.test(method.invoke(finstance, (Object)null));
                  } catch (IllegalAccessException | InvocationTargetException e) {
                     throw new RuntimeException(e);
                  }
               };
            }
            case 1 -> {
               if(String[].class.isAssignableFrom(parameters[0].getType())) {
                  command = (sender, args) -> {
                     try {
                        return getRet.test(method.invoke(finstance, (Object) args));
                     } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                     }
                  };
               } else if(Player.class.isAssignableFrom(parameters[0].getType())) {
                  allowConsole = true;
                  command = (sender, args) -> {
                     try {
                        return getRet.test(method.invoke(finstance, (Player) sender));
                     } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                     }
                  };
               } else if(CommandSender.class.isAssignableFrom(parameters[0].getType())) {
                  command = (sender, args) -> {
                     try {
                        return getRet.test(method.invoke(finstance, sender));
                     } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                     }
                  };
               } else {
                  log.doSevere("Method " + method.getName() + " of class " + commandClass.getName() + " has wrong parameter type: " + parameters[0].getType());
                  log.doWarn("Expected one of " + CommandSender.class + " or " + String[].class);
               }
            }
            case 2 -> {
               if(!(String[].class.isAssignableFrom(parameters[0].getType()) || String[].class.isAssignableFrom(parameters[1].getType()))) {
                  log.doSevere("Method " + method.getName() + " of class " + commandClass.getName() + " does not have a parameter of type " + String[].class);
               }
               if(Player.class.isAssignableFrom(parameters[0].getType())) {
                  allowConsole = true;
                  command = (sender, args) -> {
                     try {
                        return getRet.test(method.invoke(finstance, (Player) sender, args));
                     } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                     }
                  };
               } else if(CommandSender.class.isAssignableFrom(parameters[0].getType())) {
                  command = (sender, args) -> {
                     try {
                        return getRet.test(method.invoke(finstance, sender, args));
                     } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                     }
                  };
               } else if(Player.class.isAssignableFrom(parameters[1].getType())) {
                  allowConsole = true;
                  command = (sender, args) -> {
                     try {
                        return getRet.test(method.invoke(finstance, args, (Player) sender));
                     } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                     }
                  };
               } else if(CommandSender.class.isAssignableFrom(parameters[1].getType())) {
                  command = (sender, args) -> {
                     try {
                        return getRet.test(method.invoke(finstance, args, sender));
                     } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                     }
                  };
               } else {
                  log.doSevere("Method " + method.getName() + " of class " + commandClass.getName() + " does not have a parameter of type " + CommandSender.class);
               }
            }
         }
         break;
      }
      
      if(command == null) {
         Log.getLog(commandClass).doSevere("No method found that took 2 parameters or less of the right type, skipping");
         return;
      }
      
      String usage = annotation.usage().isBlank() ?  "/" + annotation.name() : annotation.usage();
      CustomCommand newCommand = new CustomCommand(annotation.name(), annotation.description(), usage, List.of(annotation.aliases()),
          command, List.of(annotation.permission()), allowConsole || annotation.consoleAllowed());
      if(annotation.parent() != NullCommand.class) {
         Optional<ICommand> parentCommand = commands.stream().filter(c -> c.getClass() == annotation.parent()).findAny();
         if(parentCommand.isEmpty()) {
            try {
               registerCommand(annotation.parent());
               parentCommand = commands.stream().filter(c -> c.getClass() == annotation.parent()).findAny();
            } catch (RuntimeException ignored) {}
         }
         if(parentCommand.isPresent() && parentCommand.get() instanceof CustomCommand cc)
            cc.addSubcommand(newCommand);
      }
      commands.add(newCommand);
   }
   
   /**
    * Register all commands associated with this instance to the bukkit server
    * <p>
    * This method should only be called once per instance
    */
   public void registerCommands() {
      String prefix = plugin.getDescription().getPrefix() == null ? plugin.getDescription().getName() : plugin.getDescription().getPrefix();
      List<org.bukkit.command.Command> command = new ArrayList<>(commands.size());
      commands.forEach((cmd) -> {
         if(cmd instanceof org.bukkit.command.Command) command.add((org.bukkit.command.Command) cmd);
         else command.add(new BukkitCommand(cmd.getName(), cmd.getDescription(), cmd.getUsageMessage(), cmd.getAliases()) {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
               return cmd.onCommand(sender, args, 0);
            }
            
            @Override
            public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
               return cmd.onTabComplete(sender, args, 0);
            }
         });
      });
      
      Bukkit.getServer().getCommandMap().registerAll(prefix, command);
   }
   
   /**
    * Default handler for {@link ICommand#getMessage(ICommand.Reason) error message}
    */
   public static class ErrorMessageHandler {
      private static final Map<ICommand.Reason, String> messages = new HashMap<>(ICommand.Reason.values().length);
      
      /**
       *
       * @param reason the reason of the error
       * @return the message associated with the give error
       */
      public static String reason(ICommand.Reason reason) {
         return messages.get(reason);
      }
      
      /**
       * Load the messages from a specified section
       * <p>
       * This method is called from this plugin and should never be called
       *
       * @param section the messages to use
       */
      public static void load(ConfigurationSection section) {
         for (ICommand.Reason reason : ICommand.Reason.values()) {
            messages.putIfAbsent(reason, section.getString(reason.name().toLowerCase(), "Command not available: " + reason.ordinal()));
         }
      }
   }
}
