package sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.org.iudx.adaptor.datatypes.Message;
import org.apache.calcite.adapter.file.JsonEnumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class CustomJsonEnumerator extends JsonEnumerator {
    public CustomJsonEnumerator(List<Object> list) {
        super(list);
    }

    static JsonDataConverter deduceRowType(RelDataTypeFactory typeFactory, List<Message> list) throws JsonProcessingException {
        Message msg = list.get(0);
        final ObjectMapper objectMapper = new ObjectMapper();
        Map jsonObj = objectMapper.readValue(msg.body, Map.class);
        final List<RelDataType> types = new ArrayList<>(jsonObj.size());
        final List<String> names = new ArrayList<>(jsonObj.size());
        for (Object key : jsonObj.keySet()) {
            final RelDataType type = typeFactory.createJavaType(jsonObj.get(key).getClass());
            names.add(key.toString());
            types.add(type);
        }

//        System.out.println(names);
//        System.out.println(types);

        RelDataType relDataType = typeFactory.createStructType(Pair.zip(names, types));
        return new JsonDataConverter(relDataType, list);
    }
}
