package in.org.iudx.adaptor.sql;

import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.util.Pair;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Enumerator that reads from a Object List.
 */
public class JsonEnumerator implements Enumerator<@Nullable Object[]> {

  private final Enumerator<@Nullable Object[]> enumerator;

  public JsonEnumerator(List<LinkedHashMap<String, Object>> list) {
    List<@Nullable Object[]> objs = new ArrayList<>();
    for (LinkedHashMap<String, Object> obj : list) {
      objs.add((obj).values().toArray());
    }
    enumerator = Linq4j.enumerator(objs);
  }

  /** Deduces the names and types of a table's columns by reading the first line
   * of a JSON file. */
  static JsonDataConverter deduceRowType(RelDataTypeFactory typeFactory, List<LinkedHashMap<String, Object>> listSource) {
    LinkedHashMap<String, Object> jsonFieldMap;
    jsonFieldMap = listSource.get(0);

    final List<RelDataType> types = new ArrayList<>(jsonFieldMap.size());
    final List<String> names = new ArrayList<>(jsonFieldMap.size());

    for (Object key : jsonFieldMap.keySet()) {
      final RelDataType type = typeFactory.createJavaType(jsonFieldMap.get(key).getClass());
      names.add(key.toString());
      types.add(type);
    }

    RelDataType relDataType = typeFactory.createStructType(Pair.zip(names, types));
    return new JsonDataConverter(relDataType, listSource);
  }

  @Override public Object[] current() {
    return enumerator.current();
  }

  @Override public boolean moveNext() {
    return enumerator.moveNext();
  }

  @Override public void reset() {
    enumerator.reset();
  }

  @Override public void close() {
    enumerator.close();
  }

  /**
   * Json data and relDataType Converter.
   */
  static class JsonDataConverter {
    private final RelDataType relDataType;
    private final List<LinkedHashMap<String, Object>> dataList;

    private JsonDataConverter(RelDataType relDataType, List<LinkedHashMap<String, Object>> dataList) {
      this.relDataType = relDataType;
      this.dataList = dataList;
    }

    RelDataType getRelDataType() {
      return relDataType;
    }

    List<LinkedHashMap<String, Object>> getDataList() {
      return dataList;
    }
  }
}
