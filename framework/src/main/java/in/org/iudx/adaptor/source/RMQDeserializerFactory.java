package in.org.iudx.adaptor.source;

import in.org.iudx.adaptor.codegen.Parser;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.datatypes.Rule;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.connectors.rabbitmq.RMQDeserializationSchema;


@SuppressWarnings("rawtypes")
public class RMQDeserializerFactory<T> {

  public static RMQDeserializationSchema getDeserializer(TypeInformation typeinfo,
                                                         String appName, String routingKey, Parser parser) {
    if (typeinfo.getTypeClass().equals(Message.class)) {
      return new RMQMessageDeserializer(appName, routingKey, parser);
    }
    if (typeinfo.getTypeClass().equals(Rule.class)) {
      return new RMQRuleDeserializer(appName, parser);
    }
    return null;
  }
}
