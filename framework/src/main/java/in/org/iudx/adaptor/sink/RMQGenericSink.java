package in.org.iudx.adaptor.sink;

import in.org.iudx.adaptor.codegen.RMQConfig;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.datatypes.RuleResult;
import in.org.iudx.adaptor.logger.CustomLogger;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSink;


/**
 * Extends Flink native RMQSink {@link RMQSink}
 * This is a wrapper to extend functionality to RMQSink for posterity.
 */
public class RMQGenericSink<T> extends RMQSink<T> {

  transient CustomLogger logger;
  private TypeInformation typeInformation;
  private transient Counter counter;

  public RMQGenericSink(RMQConfig rmqConfig, TypeInformation typeInfo) {
    super(rmqConfig.build(), new RMQSerializerFactory<>().getSerializer(typeInfo),
            new RMQPublisherFactory<>().getPublisher(typeInfo, rmqConfig));
    this.typeInformation= typeInfo;
  }

  @Override
  public void open(Configuration config) throws Exception {
    super.open(config);

    ExecutionConfig.GlobalJobParameters parameters = getRuntimeContext().getExecutionConfig()
            .getGlobalJobParameters();
    String appName = parameters.toMap().get("appName");
    logger = new CustomLogger(RMQGenericSink.class, appName);

    this.counter = getRuntimeContext().getMetricGroup().counter("RMQSinkCounter");
  }

  @Override
  public void invoke(T value, Context context) throws Exception {
    System.out.println(typeInformation.getTypeClass());
    try {
      if (typeInformation.getTypeClass().equals(Message.class)) {
        Message data = (Message) value;
        logger.debug("[event_key - " + data.key + "] publishing event to sink");
      }
      if (typeInformation.getTypeClass().equals(RuleResult.class)) {
        RuleResult data = (RuleResult) value;
        logger.debug("[routing_key - " + data.sinkRoutingKey + "] Publishing alert");
      }

      super.invoke(value, context);
      this.counter.inc();
    } catch (Exception e) {
      logger.error("Failed to publish to sink", e);
      throw e;
    }
  }
}
