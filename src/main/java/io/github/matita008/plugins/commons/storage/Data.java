package io.github.matita008.plugins.commons.storage;

import java.util.List;

/**
 * Interface representing a class that can be saved by {@link Storage}
 * <p>
 * Each class that implements this interface shall have a single field or method annotated with {@link Loader}
 * In case multiple fields/methods are found is implementation-dependant which one to use
 */
public interface Data {
   List<Value> getData();
   
   interface Builder {
      /**
       * Gets the memory representation of the object this builder returns
       *
       * @return a list of {@link Key Keys}
       */
      List<Key> getTypes();
      
      /**
       * Gets the primary key for each table
       *
       * @return a list containing a {@link Key} entry for each table
       */
      List<Key> getKeys();
      
      /**
       * Creates a {@link Data} object based on the provided input data and the specified table name.
       *
       * @param data a map associating {@link Data.Value} instances with their corresponding values,
       *             containing the data to be encapsulated in the created {@link Data} object
       * @return a {@link Data} object representing the provided data for the specified table
       */
      Data create(List<Value> data);
   }
   
   /**
    * Represent the type of data entry
    */
   enum Type{
      STRING,
      INTEGER,
      DOUBLE,
      BOOLEAN,
      LONG,
      FLOAT,
      UUID
   }
   
   /**
    * Represents a column associated with a specific table in a data context.
    * <p>
    * This record contains the type of the data and the name of the
    * table to which the key belongs.
    *
    * @param table the name of the table with which the key is associated
    * @param type  the type of data for the key, represented by {@link Data.Type}
    * @param name the name of this column
    */
   record Key(String table, Type type, String name) { }
   
   record Value(Key key, Object data) {
      public Type getType() { return key.type; }
   }
}
