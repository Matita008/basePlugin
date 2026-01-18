package io.github.matita008.plugins.commons.storage;

import java.util.Map;

public interface Data {
   Map<String, String> getData();
   Map<String, Type> getTypes();
   
   enum Type{
      String,
      Integer,
      Double,
      Boolean,
      Long,
      Float,
      UUID
   }
}
