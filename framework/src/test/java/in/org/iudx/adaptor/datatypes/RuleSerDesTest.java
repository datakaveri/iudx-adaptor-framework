package in.org.iudx.adaptor.datatypes;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class RuleSerDesTest {


  @Test
  void testSer() throws Exception {
    String data =  "{\"ruleId\":1," +
            "\"sqlQuery\":\"SELECT * FROM (SELECT * FROM TABLE ORDER BY " +
            "observationDateTime DESC LIMIT 1 WHERE speed > 30)\"," +
            "\"type\":\"RULE\",\"windowMinutes\": 1000," +
            "\"sinkExchangeKey\": \"test\",\"sinkRoutingKey\": \"test\"}";

    ObjectMapper mapper = new ObjectMapper();
    ObjectReader reader = mapper.readerFor(Rule.class).with(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
    Rule rule = mapper.readValue(data, Rule.class);
    System.out.println(rule.toString());
  }
}
