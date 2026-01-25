package io.github.matita008.plugins.commons.commands;

import io.github.matita008.plugins.commons.logging.Log;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface ICommand extends CommandExecutor, TabCompleter {
   /** Returns the name of this command
    * @return Name of this command
    */
   @NotNull String getName();
   /** Gets a brief description of this command
    * @return Description of this command
    */
   @NotNull String getDescription();
   /** Gets an example usage of this command
    * <p>
    * the default implementation return '/' + the name of the command
    * @return One or more example usages
    */
   default @NotNull String getUsageMessage() {
      return "Usage: /" + getName();
   }
   
   /**
    * Gets the subcommands of this command
    * @return The list of subcommands
    */
   @NotNull List<ICommand> getSubcommands();
   
   /**
    * Returns a list of aliases of this command
    * @return List of aliases
    */
   @NotNull List<String> getAliases();
   
   /**
    * Returns a list of permission for this command
    * @return List of permission
    */
  @NotNull List<String> getPermissions();
   
   
   /**
    * Returns false if the command must be run from a player.
    * <p>
    * If this method returns false {@link #execute(CommandSender, String[]) the sender in execute()} can be safely casted to {@link Player}
    * @return if the console is allowed
    */
   boolean isConsoleAllowed();
   
   /**Requests a list of possible completions for a command argument.
    *
    * @param sender Source of the command.  For players tab-completing a
    *     command inside of a command block, this will be the player, not
    *     the command block.
    * @param args The arguments passed to the command, including final
    *     partial argument to be completed, excluding the command name
    * @return A List of possible completions for the final argument, or null
    *     to default to the command executor
    */
   default @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
      if(getSubcommands().isEmpty()) return List.of();
      List<String> ret = new ArrayList<>();
      
      for (ICommand c: getSubcommands()) {
         if(StringUtil.startsWithIgnoreCase(c.getName(), args[0]) || (!getAliases().isEmpty() &&
             getAliases().stream().anyMatch(a -> StringUtil.startsWithIgnoreCase(a, args[0])))) {
            ret.add(c.getName());
         }
      }
      return ret;
   }

   /**
    * Executes the given command, returning its success.
    * If false is returned, then {@link #getUsageMessage() the usage} will be sent to the player.
    *
    * @param sender The sender, can be safely casted to {@link Player} if {@link #isConsoleAllowed() the usage from the console} is disallowed
    * @param args Passed command arguments, without the subcommand name if this is run as a subcommand
    * @return true if a valid command, otherwise false
    */
   boolean execute(@NotNull CommandSender sender, String[] args);
   
   //---- Start internal stuff ----
   
   @Override
   default boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      return onCommand(sender, args, 0);
   }
   
   /**
    * Internal function to check each subcommand
    * @param sender the sender to pass
    * @param args the args to pass
    * @param depth the current depth
    * @return the return value of this command, the first subcommand or true
    */
   default boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args, int depth) {
      if(!(isConsoleAllowed() || sender instanceof Player)) {
         sender.sendMessage(getMessage(Reason.NO_CONSOLE));
         return true;
      }
      
      if(getSubcommands().isEmpty() || args.length == depth) {
         if(!isConsoleAllowed() && checkPermissions((Player) sender)) {
            sender.sendMessage(getMessage(Reason.MISSING_PERMISSION));
            return true;
         }
         
         String[] newArgs = new String[args.length-depth];
         System.arraycopy(args, depth, newArgs, 0, newArgs.length);//a small improvement could be made if depth == 0
         try {
            if(!execute(sender, newArgs)) {
               sender.sendMessage(getUsageMessage());
            }
         } catch (Exception e) {
            Log.severe("An exception occurred while executing " + getName(), e);
            sender.sendMessage(getMessage(Reason.INTERNAL_ERROR));
         }
         return true;
      }
      
      for (ICommand c: getSubcommands()) {
         if(c.getName().equalsIgnoreCase(args[depth]) || (!getAliases().isEmpty() &&
             c.getAliases().stream().anyMatch(a -> a.equalsIgnoreCase(args[depth])))) return c.onCommand(sender, args, depth+1);
      }
      
      sender.sendMessage(getMessage(Reason.MISSING_SUBCOMMAND));
      return true;
   }
   
   @Override
   default List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
      return onTabComplete(sender, args, 1);
   }
   
   /**
    * Internal function to check each subcommand and/or delegate to them
    * @param sender the sender to pass
    * @param args the args to pass
    * @param depth the current depth
    * @return a list of subcommands, the value of this command or the list returned by the correct subcommand
    */
   default List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args, int depth) {
      if(args.length-1 > depth) {
         for (ICommand c: getSubcommands()) {
            if(c.getName().equalsIgnoreCase(args[depth]) || (!getAliases().isEmpty() &&
                c.getAliases().stream().anyMatch(a -> a.equalsIgnoreCase(args[depth])))) return c.onTabComplete(sender, args, depth+1);
         }
      }
      
      String[] newArgs = new String[args.length-depth];
      System.arraycopy(args, depth, newArgs, 0, newArgs.length);//a small improvement could be made if depth == 0
      return onTabComplete(sender, newArgs);
   }
   
   /**
    * Check if a player has the permission needed to run this command.
    * If the executor is the console this method is not called
    * <p>
    * If this method return false {@link Reason#MISSING_PERMISSION the appropriate message is returned}
    * <p>
    * The default implementation check if the given player has all of the {@link #getPermissions() permission} needed
    * @param player the player to check
    * @return whenever this command is allowed to be executed by the given player
    *
    * @see #getMessage(Reason)
    */
   default boolean checkPermissions(@NotNull Player player) { return getPermissions().stream().allMatch(player::hasPermission); }
   
   /**
    * Handles cases where a command cannot be executed due to specified reasons.
    *
    * @param reason The reason the command is not allowed to execute. It must be one of {@code Reason}
    * @return A detailed error message corresponding to the specified reason
    */
   default @NotNull String getMessage(@NotNull Reason reason) { return CommandService.ErrorMessageHandler.reason(reason); }
   
   /** Possible reason a command may fail to execute */
   enum Reason {
      MISSING_PERMISSION, NO_CONSOLE, MISSING_ARGS, MISSING_SUBCOMMAND, INTERNAL_ERROR
   }
}
