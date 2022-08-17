package in.org.iudx.adaptor.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.org.iudx.adaptor.datatypes.Message;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomSchema extends AbstractSchema {
//    public List<Message> state;

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final Map<Object, ObjectNode> employees = new HashMap<>();

    static {
        employees.put(1L, mapper.createObjectNode().put("timestamp", "john").put("message", "text1"));
        employees.put(2L, mapper.createObjectNode().put("timestamp", "jane").put("message", "text2"));
        employees.put(3L, mapper.createObjectNode().put("timestamp", "cole").put("message", "text3"));
    }

    @Override
    protected Map<String, Table> getTableMap() {
        return Collections.singletonMap("employees", new CustomTable(employees));
    }
}