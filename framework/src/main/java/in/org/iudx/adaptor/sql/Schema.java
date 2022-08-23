package in.org.iudx.adaptor.sql;

import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Schema extends AbstractSchema {
    private List<LinkedHashMap> data;


    @Override
    protected Map<String, Table> getTableMap() {
        return Collections.singletonMap("state", new JsonArrayListTable(this.data));
    }

    public void setData(List<LinkedHashMap> data) {
        this.data = data;
    }
}