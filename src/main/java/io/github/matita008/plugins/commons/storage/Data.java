package io.github.matita008.plugins.commons.storage;

import java.util.List;
import java.util.UUID;

/**
 * Interface representing a class that can be saved by {@link Storage}
 * <p>
 * Each class that implements this interface shall have a single field or method annotated with {@link Loader}
 * In case multiple fields/methods are found is implementation-dependant which one to use
 */
public interface Data {
   /**
    * Retrieves a list of {@link Value} objects associated with this data.
    *
    * @return a list of {@code Value} representing this object
    */
   List<Value> getData();
   
   /**
    * Interface representing a builder for creating and managing {@link Data} objects.
    */
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
      /**Represent a String*/
      STRING,
      /**Represent an int*/
      INTEGER,
      /**Represent a double*/
      DOUBLE,
      /**Represent a boolean*/
      BOOLEAN,
      /**Represent a long*/
      LONG,
      /**Represent a float*/
      FLOAT,
      /**Represent an {@link UUID}*/
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
   record Key(String table, Type type, String name) {
      /**
       * Creates a new {@code Key} instance representing an integer column within a specified table.
       *
       * @param table the name of the table associated with the key
       * @param name the name of the column represented by the key
       * @return a {@code Key} instance with the type set to {@code Type.INTEGER}
       */
      public static Key ofInt(String table, String name) { return new Key(table, Type.INTEGER, name); }
      /**
       * Creates a new instance of {@code Key} with a {@code DOUBLE} type, representing a
       * column associated with the specified table.
       *
       * @param table the name of the table with which the key is associated
       * @param name  the name of the column
       * @return a {@code Key} instance with type {@code DOUBLE}, associated with the
       *         specified table and column name
       */
      public static Key ofDouble(String table, String name) { return new Key(table, Type.DOUBLE, name); }
      /**
       * Creates a new {@code Key} instance with a boolean data type.
       *
       * @param table the name of the table with which the key is associated
       * @param name  the name of this column
       * @return a {@code Key} instance with the specified table, a boolean data type, and column name
       */
      public static Key ofBoolean(String table, String name) {return new Key(table, Type.BOOLEAN, name);}
      /**
       * Creates a new Key instance associated with the specified table and name,
       * using the type {@code Type.LONG}.
       *
       * @param table the name of the table to which the key is associated
       * @param name the name of the column
       * @return a new Key instance with the type {@code Type.LONG}, associated with the specified table and name
       */
      public static Key ofLong(String table, String name) {return new Key(table, Type.LONG, name);}
      /**
       * Creates a new instance of {@code Key} with a type of {@code FLOAT}.
       *
       * @param table the name of the table to which the key is associated
       * @param name the name of the column to which the key corresponds
       * @return a {@code Key} instance representing a column with type {@code FLOAT}
       */
      public static Key ofFloat(String table, String name) {return new Key(table, Type.FLOAT, name);}
      /**
       * Creates a new key associated with the given table and name, with a {@code UUID} type.
       *
       * @param table the name of the table with which the key is associated
       * @param name the name of this column
       * @return a {@code Key} instance with the specified table, {@code UUID} type, and name
       */
      public static Key ofUUID(String table, String name) {return new Key(table, Type.UUID, name);}
      /**
       * Creates a new Key instance using the provided table name, type, and key name.
       *
       * @param table the name of the table to which the key belongs
       * @param name the name of the key
       * @return a new Key instance with the specified table and name
       */
      public static Key ofString(String table, String name) { return new Key(table, Type.STRING, name); }
   }
   
   /**
    * Represents a value associated with a specific key.
    *
    * @param key the {@link Key} associated with this value.
    * @param data the actual data corresponding to the specified key.
    */
   record Value(Key key, Object data) {
      /**
       * Utility function.
       * Returns the same as {@code key().type()}
       *
       * @return the {@link Type} associated with this Value.
       */
      public Type getType() { return key.type; }
   }
}
