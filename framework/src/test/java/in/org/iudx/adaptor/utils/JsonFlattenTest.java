package in.org.iudx.adaptor.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ValueNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

class JsonFlattenTest {

    @Test
    void testJacksonFlatten() throws InterruptedException, IOException {
        String json = "{\"a\":{\"b\":123,\"c\":\"xyjfj\"},\"hb\":{\"jdn\":{\"uf\":{\"jfn\":\"jfn\"},\"jdn\":{\"ujf\":\"jfnmd\"}},\"ufn\":[\"djnf\",\"jfkj\",\"jfn\"],\"jdj\":[{\"jfn\":\"jkn\",\"j\":\"jmm\"},{\"jdb\":\"jfn\",\"jdn\":1245}]},\"ujjn\":[{\"ujd\":38989,\"dkm\":8903},{\"ijj\":930,\"yhn\":7820}]}";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonObj = mapper.readTree(json);

        Map<String, Object> map = new JsonFlatten(jsonObj).flatten();

        System.out.println(map);
    }
}
