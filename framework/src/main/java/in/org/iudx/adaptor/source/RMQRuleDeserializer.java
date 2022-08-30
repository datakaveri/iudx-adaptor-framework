package in.org.iudx.adaptor.source;


import org.apache.flink.streaming.connectors.rabbitmq.RMQDeserializationSchema;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.TypeHint;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

import in.org.iudx.adaptor.logger.CustomLogger;
import in.org.iudx.adaptor.datatypes.Rule;

import com.fasterxml.jackson.databind.ObjectMapper;


public class RMQRuleDeserializer extends JsonPathParser<Rule>
                                implements RMQDeserializationSchema<Rule> {

  private ObjectMapper mapper;
  private String appName;
  transient CustomLogger logger;
  private String parseSpec;


  public RMQRuleDeserializer(String appName, String parseSpec) {
    super(parseSpec);
    this.appName = appName;
    this.parseSpec = parseSpec;
  }

  @Override
  public void deserialize(Envelope envelope,
                          BasicProperties properties,
                          byte[] body,
                          RMQDeserializationSchema.RMQCollector<Rule> collector) {

    try {
      //TODO: Use envelope to get routing key if needed
      Rule rule = mapper.readValue(body, Rule.class);
      collector.collect(rule);
    } catch (Exception e) {
      logger.error(e);
    }
  }

  @Override
  public boolean isEndOfStream(Rule nextElement) {
    return false;
  }

  @Override
  public void open(DeserializationSchema.InitializationContext context) {
    logger = new CustomLogger(HttpSource.class, appName);
    mapper = new ObjectMapper();
  }

  @Override
  public  TypeInformation<Rule> getProducedType() {
    return TypeInformation.of(new TypeHint<Rule>(){});
  }


  public RMQRuleDeserializer setAppName(String appName) {
    this.appName = appName;
    return this;
  }

  public String getAppName() {
    return appName;
  }

}
