package in.org.iudx.adaptor.sql;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.Statistic;
import org.apache.calcite.schema.Statistics;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.util.Source;
import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.schema.ScannableTable;

import org.checkerframework.checker.nullness.qual.Nullable;

import in.org.iudx.adaptor.sql.JsonEnumerator.JsonDataConverter;

import java.util.List;
import java.util.LinkedHashMap;

/**
 * Table based on a JSON file.
 */
public class JsonArrayListTable extends AbstractTable implements ScannableTable{
  private List<LinkedHashMap<String, Object>> source;
  private @Nullable RelDataType rowType;
  protected @Nullable List<LinkedHashMap<String, Object>> dataList;

  public JsonArrayListTable(List<LinkedHashMap<String, Object>> source) {
    this.source = source;
  }

  @Override public RelDataType getRowType(RelDataTypeFactory typeFactory) {
    if (rowType == null) {
      rowType = JsonEnumerator.deduceRowType(typeFactory, source).getRelDataType();
    }
    return rowType;
  }

  /** Returns the data list of the table. */
  public List<LinkedHashMap<String, Object>> getDataList(RelDataTypeFactory typeFactory) {
    if (dataList == null) {
      JsonDataConverter jsonDataConverter =
          JsonEnumerator.deduceRowType(typeFactory, source);
      dataList = jsonDataConverter.getDataList();
    }
    return dataList;
  }

  @Override public Statistic getStatistic() {
    return Statistics.UNKNOWN;
  }

  @Override public String toString() {
    return "JsonScannableTable";
  }

  @Override public Enumerable<@Nullable Object[]> scan(DataContext root) {
    return new AbstractEnumerable<@Nullable Object[]>() {
      @Override public Enumerator<@Nullable Object[]> enumerator() {
        JavaTypeFactory typeFactory = root.getTypeFactory();
        return new JsonEnumerator(getDataList(typeFactory));
      }
    };
  }

}
