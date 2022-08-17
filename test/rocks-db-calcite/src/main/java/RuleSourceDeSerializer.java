import datatypes.Rule;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;


public class RuleSourceDeSerializer  extends RichFlatMapFunction<String, Rule> {
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
    }

    @Override
    public void flatMap(String value, Collector<Rule> out) throws Exception {
        System.out.println(value);
        try {
            Rule rule = new Rule(value);
            out.collect(rule);
        } catch (Exception e) {
            System.out.println("Failed parsing rule, dropping it:" + value);
        }
    }
}
