package in.org.iudx.adaptor.source;


import org.apache.flink.streaming.connectors.rabbitmq.RMQDeserializationSchema;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.TypeHint;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

import in.org.iudx.adaptor.logger.CustomLogger;
import in.org.iudx.adaptor.datatypes.Message;
import org.json.JSONObject;

import java.util.Objects;


public class RMQMessageDeserializer extends JsonPathParser<Message>
                                  implements RMQDeserializationSchema<Message> {

  private String appName;
  transient CustomLogger logger;
  private long expiry = Integer.MIN_VALUE;
  private String routingKey;


  public RMQMessageDeserializer(String appName, String routingKey, String parseSpec) {
    super(parseSpec);
    this.appName = appName;
    this.routingKey = routingKey;
    if (parseSpec != null && !parseSpec.isEmpty()) {
     JSONObject parseSpecObj = new JSONObject(parseSpec);

     if (parseSpecObj.has("expiry")) {
      this.expiry = parseSpecObj.getLong("expiry");
     }
    }
  }

  @Override
  public void deserialize(Envelope envelope, BasicProperties properties, byte[] body,
                          RMQDeserializationSchema.RMQCollector<Message> collector) {
    try {
      Message msg = super.parse(new String(body)).setExpiry(expiry);
      if (routingKey.isEmpty()) {
        collector.collect(msg);
      } else {
        if (Objects.equals(envelope.getRoutingKey(), routingKey)) {
          collector.collect(msg);
        }
      }

    } catch (Exception e) {
      logger.error(e);
    }
  }

  @Override
  public boolean isEndOfStream(Message nextElement) {
    return false;
  }

  @Override
  public void open(DeserializationSchema.InitializationContext context) {
  }

  @Override
  public  TypeInformation<Message> getProducedType() {
    return TypeInformation.of(new TypeHint<Message>() {});
  }

  public RMQMessageDeserializer setAppName(String appName) {
    this.appName = appName;
    return this;
  }

  public String getAppName() {
    return appName;
  }
}
