package in.org.iudx.adaptor.source;

import in.org.iudx.adaptor.logger.CustomLogger;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.datatypes.Rule;
import in.org.iudx.adaptor.codegen.RMQSourceConfig;

import org.apache.flink.streaming.connectors.rabbitmq.RMQSource;
import org.apache.flink.streaming.connectors.rabbitmq.RMQDeserializationSchema;
import org.apache.flink.configuration.Configuration;



/**
 * {@link RMQSource} - The RMQSource Class
 * <p>
 * This generic function exchanges meesages as {@link Message} objects.
 * <p>
 * T - Parser Output
 * <p>
 * Notes:
 * - ?This is serializable from flink examples
 */
public class RMQGenericSource<T> extends RMQSource<T> {

  private static final long serialVersionUID = 1L;
  private RMQSourceConfig rmqConfig;
  private RMQDeserializationSchema<T> deser;

  transient CustomLogger logger;

  public RMQGenericSource(RMQSourceConfig rmqConfig, RMQDeserializationSchema<T> deser) {
    super(rmqConfig.build(), rmqConfig.getQueueName(), deser);
    this.deser = deser;
    this.rmqConfig = rmqConfig;
  }

  @Override
  public void open(Configuration config) throws Exception {
    super.open(config);
  }

}
