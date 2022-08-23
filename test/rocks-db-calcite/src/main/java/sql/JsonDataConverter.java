package sql;

import in.org.iudx.adaptor.datatypes.Message;
import org.apache.calcite.rel.type.RelDataType;

import java.util.List;

public class JsonDataConverter {
    private final RelDataType relDataType;
    private final List<Message> dataList;

    public JsonDataConverter(RelDataType relDataType, List<Message> dataList) {
        this.relDataType = relDataType;
        this.dataList = dataList;
    }

    RelDataType getRelDataType() {
        return relDataType;
    }

    List<Message> getDataList() {
//        System.out.println(dataList);
        return dataList;
    }
}
