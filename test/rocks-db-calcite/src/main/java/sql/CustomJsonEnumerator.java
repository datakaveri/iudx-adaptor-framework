package sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.org.iudx.adaptor.datatypes.Message;
import org.apache.calcite.adapter.file.JsonEnumerator;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.util.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class CustomJsonEnumerator implements Enumerator<@Nullable Object[]> {
    private final Enumerator<@Nullable Object[]> enumerator;
    public CustomJsonEnumerator(List<Message> list) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        List<@Nullable Object[]> objs = new ArrayList<>();
        for (Message msg : list) {
            LinkedHashMap jsonObj = objectMapper.readValue(msg.body, LinkedHashMap.class);
            objs.add((jsonObj).values().toArray());
        }
        enumerator = Linq4j.enumerator(objs);
    }

    static JsonDataConverter deduceRowType(RelDataTypeFactory typeFactory, List<Message> list) throws JsonProcessingException {
        Message msg = list.get(0);
        final ObjectMapper objectMapper = new ObjectMapper();
        LinkedHashMap jsonObj = objectMapper.readValue(msg.body, LinkedHashMap.class);
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
}
