package in.org.iudx.adaptor.source;

import in.org.iudx.adaptor.codegen.RMQConfig;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.logger.CustomLogger;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource;


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
  transient CustomLogger logger;
  private RMQConfig rmqConfig;

  public RMQGenericSource(RMQConfig rmqConfig, TypeInformation typeInformation,
                          String appName, String parseSpec) {
    super(rmqConfig.build(), rmqConfig.getQueueName(), false,
            new RMQDeserializerFactory<>().getDeserializer(typeInformation, appName, rmqConfig.getRoutingKey(), parseSpec));
    this.rmqConfig = rmqConfig;
  }

  @Override
  public void open(Configuration config) throws Exception {
    super.open(config);
  }

  @Override
  protected void setupQueue() {
  }

}
