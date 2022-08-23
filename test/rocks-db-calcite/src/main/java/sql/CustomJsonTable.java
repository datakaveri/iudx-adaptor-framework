package sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import in.org.iudx.adaptor.datatypes.Message;
import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.file.JsonEnumerator;
import org.apache.calcite.adapter.file.JsonTable;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.Statistic;
import org.apache.calcite.schema.Statistics;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.util.Source;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.List;

public class CustomJsonTable extends AbstractTable implements ScannableTable {
    private final List<Message> sourceData;
    private @Nullable RelDataType rowType;
    protected @Nullable List<Message> dataList;

    public CustomJsonTable(List<Message> list) {
        this.sourceData = list;
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory relDataTypeFactory) {
        if (rowType == null) {
            try {
                rowType = CustomJsonEnumerator.deduceRowType(relDataTypeFactory, sourceData).getRelDataType();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return rowType;
    }

    public List<Message> getDataList(RelDataTypeFactory typeFactory) throws JsonProcessingException {
        if (dataList == null) {
            JsonDataConverter jsonDataConverter = CustomJsonEnumerator.deduceRowType(typeFactory, sourceData);
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
        return new AbstractEnumerable<Object[]>() {
            @Override public Enumerator<@Nullable Object[]> enumerator() {
                JavaTypeFactory typeFactory = root.getTypeFactory();
                try {
                    return new CustomJsonEnumerator(getDataList(typeFactory));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
