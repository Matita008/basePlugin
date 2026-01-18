package io.github.matita008.plugins.commons.storage.implementations;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.matita008.plugins.commons.PluginBase;
import io.github.matita008.plugins.commons.logging.Log;
import io.github.matita008.plugins.commons.storage.Data;
import io.github.matita008.plugins.commons.storage.Database;
import io.github.matita008.plugins.commons.storage.StorageManager;
import lombok.*;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HikariDatabase extends Database {
   private HikariDataSource dataSource;
   private static final String LOG_PREFIX = "HikariDB";
   private final Log logger = Log.getLog(getPlugin()).addPrefix(LOG_PREFIX);
   
   public HikariDatabase(PluginBase plugin) {
      super(plugin);
   }
   
   @Override
   public void init(ConfigurationSection config) {
      HikariConfig hikariConfig = new HikariConfig();
      hikariConfig.setJdbcUrl(config.getString("url", "jdbc:h2:" + plugin.getDataFolder().getAbsolutePath() + "/db"));
      hikariConfig.setUsername(config.getString("username", "root"));
      hikariConfig.setPassword(config.getString("password", ""));
      hikariConfig.setMaximumPoolSize(config.getInt("pool-size", 2));
      
      try {
         dataSource = new HikariDataSource(hikariConfig);
         createTables();
      } catch (Exception e) {
         logger.doSevere("Failed to initialize database connection pool", e);
      }
   }
   
   @Override
   public void save(Data data) {
      if(data == null) return;
      
      try (Connection conn = getConnection()) {
         if(conn != null) {
            Data.Builder builder = getBuilder(data.getClass());
            if(builder == null) return;
            
            for (Data.Key type: builder.getTypes()) {
               String table = type.table();
               List<Data.Value> values = data.getData();
               if(values == null || values.isEmpty()) continue;
               
               List<String> pkColumns = builder.getKeys().stream()
                                               .filter(k->k.table().equals(table))
                                               .map(Data.Key::name)
                                               .toList();
               
               StringBuilder sql = new StringBuilder("MERGE INTO ");
               sql.append(table);
               sql.append(" USING (VALUES (");
               
               sql.append("?,".repeat(values.size()));
               sql.setLength(sql.length() - 1); // Remove trailing comma
               sql.append(")) AS SOURCE (");
               
               for (Data.Value value: values) {
                  sql.append(value.key().name()).append(",");
               }
               sql.setLength(sql.length() - 1);
               
               // Add ON clause for primary keys
               sql.append(") ON ");
               for (int i = 0; i < pkColumns.size(); i++) {
                  if(i > 0) sql.append(" AND ");
                  sql.append(table).append(".").append(pkColumns.get(i))
                     .append("=SOURCE.").append(pkColumns.get(i));
               }
               
               sql.append(" WHEN MATCHED THEN UPDATE SET ");
               for (Data.Value value : values) {
                  if(!pkColumns.contains(value.key().name())) {
                     sql.append(value.key().name()).append("=SOURCE.").append(value.key().name()).append(",");
                  }
               }
               sql.setLength(sql.length() - 1);
               
               sql.append(" WHEN NOT MATCHED THEN INSERT (");
               sql.append(String.join(",", values.stream().map(v->v.key().name()).toList()));
               sql.append(") VALUES (");
               sql.append(String.join(",", values.stream().map(v->"SOURCE." + v.key().name()).toList()));
               sql.append(")");
               
               try (var ps = conn.prepareStatement(sql.toString())) {
                  int paramIndex = 1;
                  for (Data.Value value: values) {
                     Data.Key currentKey = builder.getTypes().stream()
                                                  .filter(k -> k.table().equals(table) && k.name().equals(value.key().name()))
                                                  .findFirst().orElse(null);
                     
                     if(currentKey != null) {
                        switch(currentKey.type()) {
                           case UUID, STRING -> ps.setString(paramIndex, value.data().toString());
                           case INTEGER -> ps.setInt(paramIndex, (Integer) value.data());
                           case LONG -> ps.setLong(paramIndex, (Long) value.data());
                           case DOUBLE -> ps.setDouble(paramIndex, (Double) value.data());
                           case BOOLEAN -> ps.setBoolean(paramIndex, (Boolean) value.data());
                           case FLOAT -> ps.setFloat(paramIndex, (Float) value.data());
                        }
                     } else {
                        ps.setString(paramIndex, value.data().toString());
                     }
                     paramIndex++;
                  }
                  ps.executeUpdate();
               }
            }
         }
      } catch (SQLException e) {
         logger.doSevere("Failed to save data", e);
      }
   }
   
   @Override
   public Data get(Class<? extends Data> clazz, List<Data.Value> key) {
      if (key == null || key.isEmpty()) return null;
      
      try (Connection conn = getConnection()) {
         if(conn != null) {
            Data.Builder builder = getBuilder(clazz);
            if(builder == null) return null;
            
            Map<String, List<Data.Value>> keysByTable = key.stream().collect(Collectors.groupingBy(v -> v.key().table()));
            List<Data.Value> resultData = new LinkedList<>();
            
            for (Data.Key type: builder.getTypes()) {
               String table = type.table();
               List<Data.Value> tableKeys = keysByTable.get(table);
               if(tableKeys == null || tableKeys.isEmpty()) continue;
               
               StringBuilder sql = new StringBuilder("SELECT * FROM ");
               sql.append(table);
               sql.append(" WHERE ");
               
               for (int i = 0; i < tableKeys.size(); i++) {
                  if(i > 0) sql.append(" AND ");
                  sql.append(tableKeys.get(i).key().name()).append("=?");
               }
               
               try (var ps = conn.prepareStatement(sql.toString())) {
                  for (int i = 0; i < tableKeys.size(); i++) {
                     ps.setObject(i + 1, tableKeys.get(i).data());
                  }
                  
                  try (var rs = ps.executeQuery()) {
                     if(rs.next()) {
                        List<Data.Key> tableColumns = builder.getTypes().stream().filter(k->k.table().equals(table)).toList();
                        for (Data.Key column: tableColumns) {
                           Object value = switch(column.type()) {
                              case UUID, STRING -> rs.getString(column.name());
                              case INTEGER -> rs.getInt(column.name());
                              case LONG -> rs.getLong(column.name());
                              case DOUBLE -> rs.getDouble(column.name());
                              case BOOLEAN -> rs.getBoolean(column.name());
                              case FLOAT -> rs.getFloat(column.name());
                           };
                           if(value != null) {
                              resultData.add(new Data.Value(column, value));
                           }
                        }
                     }
                  }
               }
            }
            if(!resultData.isEmpty()) {
               return builder.create(resultData);
            }
         }
      } catch (SQLException e) {
         logger.doSevere("Failed to retrieve data", e);
      }
      return null;
   }
   
   @Override
   protected Connection getConnection() {
      try {
         return dataSource != null ? dataSource.getConnection() : null;
      } catch (SQLException e) {
         logger.doSevere("Failed to get database connection", e);
         return null;
      }
   }
   
   @Override
   public void closeAll() {
      if (dataSource != null && !dataSource.isClosed()) {
         dataSource.close();
      }
   }
   
   private void createTables() {
      try (Connection conn = getConnection()) {
         if(conn != null) {
            
            for (val builder: StorageManager.getStorageManager(getPlugin()).getDataClasses()) {
               for (Data.Key type: getBuilder(builder).getTypes()) {
                  createTable(conn, type, getBuilder(builder));
               }
            }
         }
      } catch (SQLException e) {
         logger.doSevere("Failed to create tables", e);
      }
   }
   
   private void createTable(Connection conn, Data.Key type, Data.Builder builder) throws SQLException {
      StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
          .append(type.table())
          .append(" (");
      
      List<Data.Key> columns = builder.getTypes().stream().filter(k->k.table().equals(type.table())).toList();
      
      for (Data.Key column: columns) {
         sql.append(column.name());
         sql.append(switch(column.type()) {
            case UUID, STRING -> " VARCHAR(255)";
            case INTEGER -> " INT";
            case LONG -> " BIGINT";
            case DOUBLE -> " DOUBLE";
            case BOOLEAN -> " BOOLEAN";
            case FLOAT -> " FLOAT";
         });
         
         if(builder.getKeys().contains(column)) {
            sql.append(" PRIMARY KEY");
         }
         sql.append(",");
      }
      sql.setLength(sql.length() - 1);
      sql.append(")");
      
      try (var ps = conn.prepareStatement(sql.toString())) {
         ps.executeUpdate();
      }
   }
}
