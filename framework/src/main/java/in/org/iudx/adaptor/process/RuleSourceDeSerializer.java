package in.org.iudx.adaptor.process;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.org.iudx.adaptor.datatypes.Rule;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;


public class RuleSourceDeSerializer extends RichFlatMapFunction<String, Rule> {
  @Override
  public void flatMap(String value, Collector<Rule> out) throws Exception {
    try {
      ObjectMapper mapper = new ObjectMapper();
      Rule rule = mapper.readValue(value, Rule.class);
      out.collect(rule);
    } catch (Exception e) {
      System.out.println("Failed parsing rule, dropping it:" + value);
    }
  }
}
