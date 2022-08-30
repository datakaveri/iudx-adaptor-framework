package in.org.iudx.adaptor.source;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;


import org.json.JSONObject;
import org.json.JSONArray;

public class RMQPublisher {

  private static Connection connection;
  private static Channel channel;

  @BeforeAll
  public static void initialize() throws Exception {

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    factory.setUsername("guest");
    factory.setPassword("guest");
    connection = factory.newConnection();
    channel = connection.createChannel(); 
    channel.exchangeDeclare("adaptor-test", "direct", true);
  }

  @Test
  void sendHelloMessage() throws Exception {

    String message = "info: Hello World!";
    channel.basicPublish("adaptor-test", "test", null, message.getBytes("UTF-8"));
    System.out.println(" [x] Sent '" + message + "'");
  }

  @Test
  void sendRuleMessage() throws Exception {
    String rule = "{\"ruleId\":1,\"sqlQuery\":\"select * from TABLE where " 
                  + "`deviceId`='abc-456'\"," 
                  + "\"type\":\"RULE\",\"windowMinutes\": 1000," 
                  + "\"sinkExchangeKey\": " 
                  + "\"test\",\"sinkRoutingKey\": \"test\"}";
    channel.basicPublish("adaptor-test", "test", null, rule.getBytes("UTF-8"));
    System.out.println(" [x] Sent");
 
  }

  @Test
  void sendBadRuleMessage() throws Exception {
    String rule = "\"ruleId\":1,\"sqlQuery\":\"select * from TABLE where " 
                  + "`deviceId`='abc-456'\"," 
                  + "\"type\":\"RULE\",\"windowMinutes\": 1000," 
                  + "\"sinkExchangeKey\": " 
                  + "\"test\",\"sinkRoutingKey\": \"test\"}";
    channel.basicPublish("adaptor-test", "test", null, rule.getBytes("UTF-8"));
    System.out.println(" [x] Sent");
 
  }

  @Test
  void sendMessage() throws Exception {
    String data = new JSONObject()
      .put("time", "2021-04-01 12:00:01")
      .put("id", "123")
      .put("k", 1.5)
      .toString();
    channel.basicPublish("adaptor-test", "test", null, data.getBytes("UTF-8"));
    System.out.println(" [x] Sent");
 
  }
}