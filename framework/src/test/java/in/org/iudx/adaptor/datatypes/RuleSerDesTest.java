package in.org.iudx.adaptor.datatypes;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;




public class RuleSerDesTest {


  @Test
  void testSer() throws Exception {

    String data =  "{\"ruleId\":1,\"sqlQuery\":\"select * from TABLE where " + "`deviceId`='abc-456'\"," + "\"type\":\"RULE\",\"windowMinutes\": 1000," + "\"sinkExchangeKey\": " + "\"test\",\"sinkRoutingKey\": \"test\"}";

    ObjectMapper mapper = new ObjectMapper();
    Rule rule = mapper.readValue(data, Rule.class);

    System.out.println(rule.toString());

  }

}
