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

  private static Channel ruleChannel;

  @BeforeAll
  public static void initialize() throws Exception {

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    factory.setUsername("guest");
    factory.setPassword("guest");
    connection = factory.newConnection();
    channel = connection.createChannel(); 
    channel.exchangeDeclare("adaptor-test", "direct", true);

    ruleChannel = connection.createChannel();
    ruleChannel.exchangeDeclare("rules-test", "direct", true);
  }

  @Test
  void sendHelloMessage() throws Exception {

    String message = "info: Hello World!";
    channel.basicPublish("adaptor-test", "test", null, message.getBytes("UTF-8"));
    System.out.println(" [x] Sent '" + message + "'");
  }

  @Test
  public void sendRuleMessage() throws Exception {
    String rule = "{\"ruleId\":1,\"sqlQuery\":\"select * from TABLE where " 
                  + "`id`='123'\","
                  + "\"type\":\"RULE\",\"windowMinutes\": 1000," 
                  + "\"sinkExchangeKey\": " 
                  + "\"rule-result-test\",\"sinkRoutingKey\": \"rule-result-test\"}";
    ruleChannel.basicPublish("rules-test", "test", null, rule.getBytes("UTF-8"));
    System.out.println(" [x] Sent");
 
  }

  @Test
  public void sendDeleteRule() throws Exception {
    String rule = "{\"ruleId\":1, "
            + "\"type\":\"DELETE\"}";
    ruleChannel.basicPublish("rules-test", "test", null, rule.getBytes("UTF-8"));
    System.out.println(" [x] Sent");

  }

  @Test
  void sendBadRuleMessage() throws Exception {
    String rule = "\"ruleId\":1,\"sqlQuery\":\"select * from TABLE where " 
                  + "`k`='123'\","
                  + "\"type\":\"RULE\",\"windowMinutes\": 1000," 
                  + "\"sinkExchangeKey\": " 
                  + "\"test\",\"sinkRoutingKey\": \"test\"}";
    channel.basicPublish("rules-test", "test", null, rule.getBytes("UTF-8"));
    System.out.println(" [x] Sent");
 
  }

  @Test
  public void sendMessage(int i) throws Exception {
    String data = new JSONObject()
      .put("observationDateTime", "2021-04-01 13:00:01".replace("3", String.valueOf(i)))
      .put("id", "123")
      .put("k", 1.5)
      .toString();
    channel.basicPublish("adaptor-test", "test", null, data.getBytes("UTF-8"));
    System.out.println(" [x] Sent");
 
  }

  @Test
  void sendMessageAsync() throws Exception {
    int numMsgs = 5;
    for (int i=0;i< numMsgs;i++) {
      this.sendMessage(i);
      Thread.sleep(1000);
    }
  }
}
