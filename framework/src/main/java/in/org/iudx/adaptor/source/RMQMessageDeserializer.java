package in.org.iudx.adaptor.source;


import org.apache.flink.streaming.connectors.rabbitmq.RMQDeserializationSchema;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.TypeHint;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;
import java.text.ParseException;

import in.org.iudx.adaptor.codegen.Parser;
import in.org.iudx.adaptor.datatypes.Message;


public class RMQMessageDeserializer implements Parser<Message>, RMQDeserializationSchema<Message> {

  private String appName;

  public RMQMessageDeserializer() {
  }

  public RMQMessageDeserializer(String appName) {
    this.appName = appName;
  }

  @Override
  public void deserialize(Envelope envelope, BasicProperties properties, byte[] body, RMQDeserializationSchema.RMQCollector<Message> collector) {
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


  @Override
  public Parser<Message> initialize() {
    return this;
  }

  @Override
  public Message parse(String data) throws ParseException {
    return new Message();
  }


  public RMQMessageDeserializer setAppName(String appName) {
    this.appName = appName;
    return this;
  }

  public String getAppName() {
    return appName;
  }


}
