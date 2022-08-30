package in.org.iudx.adaptor.source;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.datatypes.Rule;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.connectors.rabbitmq.RMQDeserializationSchema;


@SuppressWarnings("rawtypes")
public class RMQDeserializerFactory<T> {

  public static RMQDeserializationSchema getDeserializer(TypeInformation typeinfo,
                                                         String appName, String parseSpec) {
    if (typeinfo.getTypeClass().equals(Message.class)) {
      return new RMQMessageDeserializer(appName, parseSpec);
    }
    if (typeinfo.getTypeClass().equals(Rule.class)) {
      return new RMQRuleDeserializer(appName, parseSpec);
    }
    return null;
  }
}
