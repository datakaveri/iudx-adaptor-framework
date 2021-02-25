package in.org.iudx.adaptor.sink;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.test.util.MiniClusterResourceConfiguration;
import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.codegen.SimpleADeduplicator;
import in.org.iudx.adaptor.codegen.SimpleATestParser;
import in.org.iudx.adaptor.codegen.SimpleATestTransformer;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.process.LokiProcessMessages;
import in.org.iudx.adaptor.source.HttpSource;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.sockjs.impl.StringEscapeUtils;
import org.apache.commons.io.IOUtils;


public class LokiSinkTest {

  public static MiniClusterWithClientResource flinkCluster;
  static JsonObject confJson = null;
  static Object confFile = null;

  @BeforeAll
  public static void initialize() {

    /* Read loki configuration file */
    try (FileInputStream inputStream = new FileInputStream("configs/mock-loki.json")) {
      confFile = IOUtils.toString(inputStream, Charset.defaultCharset());
      confJson = new JsonObject((String) confFile);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    /* inititalize flink minicluster */
    flinkCluster = new MiniClusterWithClientResource(
        new MiniClusterResourceConfiguration.Builder()
        .setNumberSlotsPerTaskManager(2)
        .setNumberTaskManagers(1).build());
  }

  @Test
  public void sidOutputSinkTest() throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
    env.setParallelism(1);
    env.enableCheckpointing(500);
    env.disableOperatorChaining();



    String lokiPaylod = confJson.getJsonObject("lokiPaylod").toString();


    SimpleATestParser parser = new SimpleATestParser();

    ApiConfig apiConfig = new ApiConfig().setUrl("http://127.0.0.1:8080/simpleA")
        .setRequestType("GET").setPollingInterval(1000L);

    ApiConfig lokiConfig = new ApiConfig()
        .setUrl(confJson.getString("host"))
        .setHeader("content-type", "application/json");
    

    DataStream<Message> messageStream = env.addSource(new HttpSource<Message>
                                                        (apiConfig, parser));

    // Generate side stream in LokiProcessMessages()
    SingleOutputStreamOperator<Tuple2<Message, Integer>> tokenize =
        messageStream.process(new LokiProcessMessages());

    tokenize.keyBy(new KeySelector<Tuple2<Message, Integer>, Integer>() {
      private static final long serialVersionUID = 1L;
      @Override
      public Integer getKey(Tuple2<Message, Integer> value) throws Exception {
        return value.f1;
      }
    });

    /* Error Sideoutput Loki */
    DataStream<String> errorSideoutput = 
      tokenize.getSideOutput(
          LokiProcessMessages.errorStream)
              .map(new MapFunction<Message, String>() {
          private static final long serialVersionUID = 1L;
          @Override
          public String map(Message value) throws Exception {
            JsonObject tempValue = new JsonObject(value.toString());
            tempValue.put("status", "error");
            return lokiPaylod
                .replace("$1", Long.toString(System.currentTimeMillis() * 1000000))
                .replace("$2",StringEscapeUtils.escapeJava(tempValue.toString()));
          }
        });

    errorSideoutput.addSink(new HttpSink(lokiConfig)).name("LokiSinkString-Error");
    errorSideoutput.print("LokiSideOutput-Error: ");

    /* Success Sideoutput Loki */
    DataStream<String> successSideoutput = tokenize.getSideOutput(
                                            LokiProcessMessages.successStream)
        .map(new MapFunction<Message, String>() {
          private static final long serialVersionUID = 1L;

          @Override
          public String map(Message value) throws Exception {
            JsonObject tempValue = new JsonObject(value.toString());
            tempValue.put("status", "success");
            return lokiPaylod
                .replace("$1", Long.toString(System.currentTimeMillis() * 1000000))
                .replace("$2",StringEscapeUtils.escapeJava(tempValue.toString()));
          }
        });

    successSideoutput.addSink(new HttpSink(lokiConfig)).name("LokiSinkString-Success");
    successSideoutput.print("LokiSideOutput-Success: ");

    env.execute(LokiSinkTest.class.getSimpleName());
  }
}
