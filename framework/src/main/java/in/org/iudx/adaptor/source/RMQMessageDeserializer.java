package in.org.iudx.adaptor.source;


import in.org.iudx.adaptor.codegen.Parser;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.connectors.rabbitmq.RMQDeserializationSchema;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.TypeHint;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

import in.org.iudx.adaptor.logger.CustomLogger;
import in.org.iudx.adaptor.datatypes.Message;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;


public class RMQMessageDeserializer<PO>
                                  implements RMQDeserializationSchema<Message> {

  private String appName;
  transient CustomLogger logger;
  private long expiry = Integer.MIN_VALUE;
  private String routingKey;

  private Parser<PO> parser;


  public RMQMessageDeserializer(String appName, String routingKey, Parser<PO> parser) {
    this.parser = parser;
    this.appName = appName;
    this.routingKey = routingKey;
  }

  @Override
  public void deserialize(Envelope envelope, BasicProperties properties, byte[] body,
                          RMQDeserializationSchema.RMQCollector<Message> collector) {
    try {
      PO msg = parser.parse(new String(body));

      try {
        /* Message array */
        if (msg instanceof ArrayList) {
          ArrayList<Message> message = (ArrayList<Message>) msg;
          for (int i = 0; i < message.size(); i++) {
            Message m = (Message) message.get(i);
            logger.debug("[event_key - " + m.key + "] Emitting event from http source");
            if (routingKey.isEmpty()) {
              collector.collect(m);
            } else {
              if (Objects.equals(envelope.getRoutingKey(), routingKey)) {
                collector.collect(m);
              }
            }
          }
        }
        /* Single object */
        if (msg instanceof Message) {
          Message m = (Message) msg;
          logger.debug("[event_key - " + m.key + "] Emitting event from http source");
          if (routingKey.isEmpty()) {
            collector.collect(m);
          } else {
            if (Objects.equals(envelope.getRoutingKey(), routingKey)) {
              collector.collect(m);
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        // Do nothing
        logger.error("[HttpSource] Error emitting source data", e);
      }


    } catch (Exception e) {
      System.out.println("error here");
      System.out.println(e.getStackTrace());
      logger.error(e);
    }
  }

  @Override
  public boolean isEndOfStream(Message nextElement) {
    return false;
  }

  @Override
  public void open(DeserializationSchema.InitializationContext context) {
    logger = new CustomLogger(RMQMessageDeserializer.class, appName);
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
