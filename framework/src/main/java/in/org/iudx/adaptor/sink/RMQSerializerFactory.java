package in.org.iudx.adaptor.sink;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.datatypes.RuleResult;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

public class RMQSerializerFactory<T> {
  public SerializationSchema getDeserializer(TypeInformation typeinfo) {
    if (typeinfo.getTypeClass().equals(Message.class)) {
      return new RMQMessageSerializer();
    }
    if (typeinfo.getTypeClass().equals(RuleResult.class)) {
      return new RMQRuleResultSerializer();
    }
    return null;
  }
}

