package io.github.matita008.plugins.commons.commands.internal;

import io.github.matita008.plugins.commons.commands.ICommand;
import io.github.matita008.plugins.commons.logging.Log;
import lombok.*;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Implementation of the Null Object Pattern for ICommand interface.
 * This class is used as a safe fallback when a command implementation is not found
 * or cannot be instantiated, preventing null pointer exceptions.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class NullCommand implements ICommand {
   @Override
   public @NotNull String getName() {
      return "";
   }
   
   @Override
   public @NotNull String getDescription() {
      return "null command";
   }
   
   @Override
   public @NotNull List<ICommand> getSubcommands() {
      return List.of();
   }
   
   @Override
   public @NotNull List<String> getAliases() {
      return List.of();
   }
   
   @Override
   public @NotNull List<String> getPermissions() {
      return List.of();
   }
   
   @Override
   public boolean isConsoleAllowed() {
      return false;
   }
   
   @Override
   public boolean execute(@NotNull CommandSender sender, String[] args) {
      Log l = Log.getLog("io.github.matita008.plugins.commons").addPrefix("Internal").addPrefix("NullCommand");
      l.doWarn("----------------------------------------------");
      l.doSevere("----------------Internal error----------------");
      l.doSevere("----------------------------------------------");
      l.doSevere("NullCommand.java got instantiated and executed");
      l.doSevere("Stacktrace: ", new Throwable());
      l.doSevere("Please warn the devs of the causing plugin");
      l.doSevere("----------------------------------------------");
      l.doWarn("----------------End of report.----------------");
      l.doWarn("----------------------------------------------");
      
      return false;
   }
}
