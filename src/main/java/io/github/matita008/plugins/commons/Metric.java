package io.github.matita008.plugins.commons;

import io.github.matita008.plugins.commons.logging.Log;
import lombok.*;

import java.util.Map;
import java.util.concurrent.Callable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Metric {
   @SuppressWarnings("unchecked") //the switch cast if fails is then logged, it should not happen
   public static void init(PluginBase plugin) {
      PluginBase.Settings settings = plugin.getSettings();
      if(settings.metricsId() < 1) return;
      
      Metrics metrics = new Metrics(plugin, settings.metricsId());
      
      for(PluginBase.Settings.Chart chart : settings.charts()) {
         try {
            switch(chart.type()) {
               case ADVANCED_BAR -> metrics.addCustomChart(new Metrics.AdvancedBarChart(chart.id(), (Callable<Map<String, int[]>>) chart.valueSupplier()));
               case ADVANCED_PIE -> metrics.addCustomChart(new Metrics.AdvancedPie(chart.id(), (Callable<Map<String, Integer>>) chart.valueSupplier()));
               case DRILLDOWN_PIE -> metrics.addCustomChart(new Metrics.DrilldownPie(chart.id(), (Callable<Map<String, Map<String, Integer>>>) chart.valueSupplier()));
               case MULTI_LINE -> metrics.addCustomChart(new Metrics.MultiLineChart(chart.id(), (Callable<Map<String, Integer>>) chart.valueSupplier()));
               case SIMPLE_BAR -> metrics.addCustomChart(new Metrics.SimpleBarChart(chart.id(), (Callable<Map<String, Integer>>) chart.valueSupplier()));
               case SIMPLE_PIE -> metrics.addCustomChart(new Metrics.SimplePie(chart.id(), (Callable<String>) chart.valueSupplier()));
               case SINGLE_LINE -> metrics.addCustomChart(new Metrics.SingleLineChart(chart.id(), (Callable<Integer>) chart.valueSupplier()));
            }
         } catch (ClassCastException cce) {
            Log.getLog(plugin).addPrefix("Metrics").doSevere("Invalid Supplier found in plugin config, skipping chart " + chart.id(), cce);
            Log.getLog(plugin).addPrefix("Metrics").doSevere("Please warn the authors of this plugin (" + plugin.getDescription().getFullName() +
                "): " + plugin.getDescription().getAuthors());
         }
      }
   }
}
