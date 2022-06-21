package in.org.iudx.adaptor.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.org.iudx.adaptor.datatypes.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class JacksonTest {

    private static final Logger LOGGER = LogManager.getLogger(JacksonTest.class);

    @BeforeAll
    public static void intialize() {
    }

    @Test
    void testJacksonStreamingParserArray() throws InterruptedException, IOException {
        String json = "[{\"key\":123,\"body\":\"Test1\"}, " + "{\"key\":456,\"body\":\"Test2\"}]";


        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        JsonFactory jfactory = new JsonFactory();
        JsonParser jsonParser = jfactory.createParser(json);

        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            jsonParser.nextToken();
            Message data = mapper.readValue(jsonParser, Message.class);
            System.out.println(data);
        }
    }

    @Test
    void testJacksonStreamingParserObject() throws InterruptedException, IOException {
        String json = "{\"key\":123,\"body\":\"Test1\"}";

        JsonFactory jfactory = new JsonFactory();
        JsonParser jsonParser = jfactory.createParser(json);

        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jsonParser.getCurrentName();
            String fieldValue = jsonParser.getText();

            System.out.println("Field name: " + fieldName + " Field value +" + fieldValue);
        }
    }
}

