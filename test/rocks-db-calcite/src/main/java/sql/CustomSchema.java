package sql;

import in.org.iudx.adaptor.datatypes.Message;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CustomSchema extends AbstractSchema {
    private List<Message> data;


    @Override
    protected Map<String, Table> getTableMap() {
        return Collections.singletonMap("state", new CustomJsonTable(this.data));
    }

    public void setData(List<Message> data) {
        this.data = data;
    }
}