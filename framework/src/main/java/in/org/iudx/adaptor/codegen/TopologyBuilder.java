package in.org.iudx.adaptor.codegen;


import org.json.JSONObject;

import com.squareup.javapoet.MethodSpec;
import javax.lang.model.element.Modifier; 
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.MethodSpec.Builder;


import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.source.HttpSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import in.org.iudx.adaptor.source.JsonPathParser;
import in.org.iudx.adaptor.process.JoltTransformer;
import in.org.iudx.adaptor.process.JSProcessFunction;
import in.org.iudx.adaptor.process.JSPathProcessFunction;
import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.process.TimeBasedDeduplicator;
import in.org.iudx.adaptor.process.GenericProcessFunction;
import in.org.iudx.adaptor.sink.AMQPSink;
import in.org.iudx.adaptor.codegen.RMQConfig;
import in.org.iudx.adaptor.process.JSTransformSpec;
import in.org.iudx.adaptor.sink.StaticStringPublisher;
import javax.annotation.processing.Filer;
import java.io.IOException;


public class TopologyBuilder {

  private TopologyConfig tc;
  private Filer filer;

  private boolean hasNonGenericTransformer;
  private boolean hasJSTransformer;
  private boolean hasJSPathTransformer;

  public TopologyBuilder(TopologyConfig config, Filer filer) {
    this.tc = config;
    this.filer = filer;
  }

  public TopologyBuilder(TopologyConfig config) {
    this.tc = config;
    this.filer = null;
  }

  public void gencode() throws IOException {

    /* Main method */
    Builder mainBuilder = MethodSpec.methodBuilder("main")
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .returns(void.class)
      .addParameter(String[].class, "args");

    /* Variables */
    mainBuilder
      .addStatement("final $T env = $T.getExecutionEnvironment()",
                      StreamExecutionEnvironment.class,
                      StreamExecutionEnvironment.class);

    inputSpecBuilder(mainBuilder, tc.inputSpec);
    parseSpecBuilder(mainBuilder, tc.parseSpec);
    deduplicationSpecBuilder(mainBuilder, tc.deduplicationSpec);
    transformSpecBuilder(mainBuilder, tc.transformSpec);
    publishSpecBuilder(mainBuilder, tc.publishSpec);
    buildTopology(mainBuilder);


    TypeSpec adaptor = TypeSpec.classBuilder("Adaptor")
      .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
      .addMethod(mainBuilder.build())
      .build();

    JavaFile javaFile = JavaFile.builder("in.org.iudx.template", adaptor)
      .build();

    if (filer != null) {
      javaFile.writeTo(filer);
    }
    else {
      javaFile.writeTo(System.out);
    }
  }


  private void inputSpecBuilder(Builder mainBuilder, JSONObject inputSpec) {

    if ("http".equals(inputSpec.getString("type"))) {
      mainBuilder
        .addStatement(
            "$T apiConfig = new $T().setUrl($S).setRequestType($S).setPollingInterval($L)", 
          ApiConfig.class, ApiConfig.class,
          inputSpec.getString("url"), inputSpec.getString("requestType"),
          inputSpec.getLong("pollingInterval"));
    }
  }

  private void parseSpecBuilder(Builder mainBuilder, JSONObject parseSpec) {

    String messageType = parseSpec.getString("type");
    mainBuilder.addStatement("String parseSpec = $S", parseSpec.toString());

    if ("json".equals(messageType)) {
      String containerType = parseSpec.getString("messageContainer");
      if ("array".equals(containerType)) {

        mainBuilder.addStatement("$T<$T[]> parser = new $T<$T[]>(parseSpec)",
                                  JsonPathParser.class, Message.class,
                                  JsonPathParser.class, Message.class);

      } else if ("single".equals(containerType)) {
        mainBuilder.addStatement("$T<$T> parser = new $T<$T>(parseSpec)",
                                  JsonPathParser.class, Message.class,
                                  JsonPathParser.class, Message.class);
      }
    }
  }

  private void deduplicationSpecBuilder(Builder mainBuilder, JSONObject dedupSpec) {
    String dedupType = dedupSpec.getString("type");

    if ("timeBased".equals(dedupType)) {
      mainBuilder.addStatement("$T dedup = new $T()",
                                TimeBasedDeduplicator.class,
                                TimeBasedDeduplicator.class);
    }
  }

  private void transformSpecBuilder(Builder mainBuilder, JSONObject transformSpec) {
    String transformType = transformSpec.getString("type");

    mainBuilder.addStatement("String transformSpec = $S", transformSpec.toString());

    if ("jolt".equals(transformType)) {
      hasNonGenericTransformer = true;
      mainBuilder.addStatement("$T trans = new $T(transformSpec)",
                                  JoltTransformer.class, JoltTransformer.class);
    }
    if ("js".equals(transformType)) {
      hasJSTransformer = true;
      hasNonGenericTransformer = false;
    }
    if ("jsPath".equals(transformType)) {
      hasJSPathTransformer = true;
      hasNonGenericTransformer = false;
    }
  }

  private void publishSpecBuilder(Builder mainBuilder, JSONObject publishSpec) {
    String publishType = publishSpec.getString("type");

    if ("rmq".equals(publishType)) {
      mainBuilder.addStatement("$T rmqConfig = new $T()",
                                RMQConfig.class, RMQConfig.class);

      mainBuilder.addStatement("rmqConfig.setPublisher(new $T($S, $S))",
                                  StaticStringPublisher.class,
                                  publishSpec.getString("sinkName"),
                                  publishSpec.getString("tagName"));
      mainBuilder.addStatement("rmqConfig.builder.setUri($S)" 
                              + ".setPort($L).setUserName($S).setPassword($S)",
                              publishSpec.getString("url"),
                              publishSpec.getInt("port"),
                              publishSpec.getString("uname"),
                              publishSpec.getString("password"));
      mainBuilder.addStatement("rmqConfig.getConfig()");

    }
  }

  private void buildTopology(Builder mainBuilder) {

    /* TODO: Parse and perform 
     * TODO: Break this construction logic further
     **/
    if (hasNonGenericTransformer) {
      mainBuilder.addStatement("$T<$T> ds = env.addSource(new $T<$T[]>(apiConfig, parser))"
                                  + ".keyBy(($T msg) -> msg.key)"
                                  + ".process(new $T(trans, dedup))",
                                SingleOutputStreamOperator.class, Message.class,
                                HttpSource.class, Message.class,
                                Message.class, GenericProcessFunction.class);
    } else {
      if (hasJSTransformer) {
        mainBuilder.addStatement("$T<$T> ds = env.addSource(new $T<$T[]>(apiConfig, parser))"
                                    + ".keyBy(($T msg) -> msg.key)"
                                    + ".process(new $T(dedup))"
                                    + ".flatMap(new $T(transformSpec))",
                                  SingleOutputStreamOperator.class, Message.class,
                                  HttpSource.class, Message.class,
                                  Message.class, GenericProcessFunction.class,
                                  JSProcessFunction.class);
      }
      if (hasJSPathTransformer) {
        mainBuilder.addStatement("$T<$T> ds = env.addSource(new $T<$T[]>(apiConfig, parser))"
                                    + ".keyBy(($T msg) -> msg.key)"
                                    + ".process(new $T(dedup))"
                                    + ".flatMap(new $T(transformSpec))",
                                  SingleOutputStreamOperator.class, Message.class,
                                  HttpSource.class, Message.class,
                                  Message.class, GenericProcessFunction.class,
                                  JSPathProcessFunction.class);
      }
    }

    mainBuilder.addStatement("$T<String> errorStream = ds.getSideOutput($T.errorStream)",
                              DataStream.class, GenericProcessFunction.class);


    /* TODO: Loki config */

    mainBuilder.addStatement("ds.addSink(new $T(rmqConfig))", AMQPSink.class);

    mainBuilder.beginControlFlow("try");
    mainBuilder.addStatement("env.execute($S)", tc.name);
    mainBuilder.nextControlFlow("catch (Exception e)"); 
    mainBuilder.endControlFlow();

  }

}
