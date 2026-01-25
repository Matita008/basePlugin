package io.github.matita008.plugins.base;

import io.github.matita008.plugins.commons.PluginBase;
import io.github.matita008.plugins.commons.commands.CommandService;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public final class ApiPlugin extends PluginBase {
   
   @Override
   public void onEnable() {
      ConfigurationSection config = getConfig();
      CommandService.ErrorMessageHandler.load(config.getConfigurationSection("messages"));
      super.onEnable();
   }
   
   @Override
   public @NotNull Settings getSettings() {
      return new Settings(29048);
   }
}
