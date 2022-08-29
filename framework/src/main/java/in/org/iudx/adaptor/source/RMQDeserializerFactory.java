package in.org.iudx.adaptor.source;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.connectors.rabbitmq.RMQDeserializationSchema;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.datatypes.Rule;


@SuppressWarnings("rawtypes")
public class RMQDeserializerFactory<T> {

  public RMQDeserializerFactory() {}

  public RMQDeserializationSchema getDeserializer(TypeInformation typeinfo) {
    if (typeinfo.getTypeClass().equals(Message.class)) {
      return (RMQDeserializationSchema) new RMQMessageDeserializer();
    }
    if (typeinfo.getTypeClass().equals(Rule.class)) {
      return (RMQDeserializationSchema) new RMQRuleDeserializer();
    } 
    return null;
  }
}
