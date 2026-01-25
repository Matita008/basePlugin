package io.github.matita008.plugins.commons.commands;

import lombok.*;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class CustomCommand extends org.bukkit.command.Command implements ICommand {
   protected BiFunction<CommandSender, String[], Boolean> command;
   @Getter List<ICommand> subcommands = new ArrayList<>();
   @Getter List<String> permissions = new ArrayList<>();
   @Getter @Setter boolean consoleAllowed = true;
   
   protected CustomCommand(@NotNull String name, @NotNull String description, @NotNull String usageMessage, @NotNull List<String> aliases) {
      super(name, description, usageMessage, aliases);
   }
   
   public CustomCommand(@NotNull String name, @NotNull String description, @NotNull String usageMessage, @NotNull List<String> aliases,
                        BiFunction<CommandSender, String[], Boolean> command) {
      super(name, description, usageMessage, aliases);
      this.command = command;
   }
   
   public CustomCommand(@NotNull String name, @NotNull String description, @NotNull String usageMessage, @NotNull List<String> aliases,
                        BiFunction<CommandSender, String[], Boolean> command, List<String> permissions) {
      super(name, description, usageMessage, aliases);
      this.command = command;
      this.permissions = permissions;
   }
   
   public CustomCommand(@NotNull String name, @NotNull String description, @NotNull String usageMessage, @NotNull List<String> aliases,
                        BiFunction<CommandSender, String[], Boolean> command, boolean consoleAllowed) {
      super(name, description, usageMessage, aliases);
      this.command = command;
      this.consoleAllowed = consoleAllowed;
   }
   
   public CustomCommand(@NotNull String name, @NotNull String description, @NotNull String usageMessage, @NotNull List<String> aliases,
                        BiFunction<CommandSender, String[], Boolean> command, List<String> permissions, boolean consoleAllowed) {
      super(name, description, usageMessage, aliases);
      this.command = command;
      this.permissions = permissions;
      this.consoleAllowed = consoleAllowed;
   }
   
   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
      return onCommand(sender, args, 0);
   }
   
   @Override
   public boolean execute(@NotNull CommandSender sender, String[] args) {
      return command.apply(sender, args);
   }
   
   public void addSubcommand(ICommand subcommand) { subcommands.add(subcommand); }
}
