package in.org.iudx.adaptor.codegen;


import in.org.iudx.adaptor.datatypes.Rule;
import in.org.iudx.adaptor.datatypes.RuleResult;
import in.org.iudx.adaptor.descriptors.RuleStateDescriptor;
import in.org.iudx.adaptor.process.*;
import in.org.iudx.adaptor.sink.RMQGenericSink;
import in.org.iudx.adaptor.source.MessageWatermarkStrategy;
import in.org.iudx.adaptor.source.RMQGenericSource;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.json.JSONObject;
import org.json.JSONArray;

import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec.Builder;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.api.java.utils.ParameterTool;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.source.HttpSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import in.org.iudx.adaptor.source.JsonPathParser;

import java.util.List;
import java.util.HashMap;
import javax.annotation.processing.Filer;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class TopologyBuilder {

    private TopologyConfig tc;
    private Filer filer;

    private boolean hasGenericTransformer;
    private boolean hasJSTransformer;
    private boolean hasJSPathTransformer;

    private static final int DEFAULT_RESTART_ATTEMPTS = 10;
    private static final long DEFAULT_RESTART_DELAY = 10000L;

    private String containerType;

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

        HashMap<String, String> propertyMap = new HashMap<String, String>();

        // TODO: Replace this with unique app name
        mainBuilder.addStatement("$T<String,String> propertyMap = new $T<String,String>()",
                HashMap.class, HashMap.class);
        mainBuilder.addStatement("propertyMap.put($S, $S)", "appName", tc.name);
        mainBuilder.addStatement("$T parameters = $T.fromMap(propertyMap)", ParameterTool.class,
                ParameterTool.class);

        /* Variables */
        mainBuilder
                .addStatement("final $T env = $T.getExecutionEnvironment()",
                        StreamExecutionEnvironment.class,
                        StreamExecutionEnvironment.class);

        // setting checkpointing
        if (!tc.isBoundedJob && tc.adaptorType != TopologyConfig.AdaptorType.RULES) {
            mainBuilder.addStatement("env.enableCheckpointing(1000 * 100 * $L)", tc.pollingInterval);
        }


        if (tc.hasFailureRecovery) {
            failureRecoverySpecBuilder(mainBuilder, tc.failureRecoverySpec, tc.inputSpec);
        } else {
            failureRecoverySpecBuilder(mainBuilder, tc.inputSpec);
        }
        inputSpecBuilder(mainBuilder, tc.inputSpec, tc.inputSourceParseSpec);

        // for RMQ source parseSpec is passed in inputSpec itself
        if (tc.adaptorType == TopologyConfig.AdaptorType.ETL && !"rmq".equals(tc.inputSpec.getString("type"))) {
            parseSpecBuilder(mainBuilder, tc.parseSpec);
        }

        if (tc.adaptorType == TopologyConfig.AdaptorType.ETL) {
            deduplicationSpecBuilder(mainBuilder, tc.deduplicationSpec);
        }

        if (tc.adaptorType == TopologyConfig.AdaptorType.ETL) {
            transformSpecBuilder(mainBuilder, tc.transformSpec);
        }

        if (tc.adaptorType == TopologyConfig.AdaptorType.RULES) {
            ruleSourceSpecBuilder(mainBuilder, tc.ruleSourceSpec, tc.ruleSourceParseSpec);
        }


        publishSpecBuilder(mainBuilder, tc.publishSpec);


        if (tc.adaptorType == TopologyConfig.AdaptorType.ETL) {
            buildTopologyForETL(mainBuilder);
        } else if (tc.adaptorType == TopologyConfig.AdaptorType.RULES) {
            buildTopologyForRules(mainBuilder);
        }


        TypeSpec adaptor = TypeSpec.classBuilder("Adaptor")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(mainBuilder.build())
                .build();

        JavaFile javaFile = JavaFile.builder("in.org.iudx.template", adaptor)
                .build();

        if (filer != null) {
            javaFile.writeTo(filer);
        } else {
            javaFile.writeTo(System.out);
        }
    }

    private void failureRecoverySpecBuilder(Builder mainBuilder, JSONObject failureRecoverySpec, JSONObject inputSpec) {

        if ("fixed-delay".equalsIgnoreCase(failureRecoverySpec.getString("type"))) {
            mainBuilder.addStatement(
                    "env.setRestartStrategy($T.fixedDelayRestart($L, $T.of($L, $T.MILLISECONDS)))",
                    RestartStrategies.class,
                    failureRecoverySpec.getInt("attempts"),
                    Time.class,
                    failureRecoverySpec.getLong("delay"),
                    TimeUnit.class);
        } else if ("exponential-delay".equalsIgnoreCase(failureRecoverySpec.getString("type"))) {
            mainBuilder.addStatement(
                    "env.setRestartStrategy($T.exponentialDelayRestart($T.of($L, $T.MILLISECONDS), $T.of($L, $T.MILLISECONDS), $L, $T.of($L, $T.MILLISECONDS), $L))",
                    RestartStrategies.class,
                    Time.class,
                    failureRecoverySpec.getLong("initial-backoff"),
                    TimeUnit.class,
                    Time.class,
                    failureRecoverySpec.getLong("max-backoff"),
                    TimeUnit.class,
                    failureRecoverySpec.getDouble("backoff-multiplier"),
                    Time.class,
                    failureRecoverySpec.getLong("reset-backoff-threshold"),
                    TimeUnit.class,
                    failureRecoverySpec.getDouble("jitter-factor"));
        }
    }

    private void failureRecoverySpecBuilder(Builder mainBuilder, JSONObject inputSpec) {

        // if failure recovery strategy not specified, use fixed-delay strategy
        // with max restarts = 10, and delay = pollingInterval (in case of streaming jobs)


        long delay = -1L;
        if (inputSpec.has("pollingInterval")) {
            delay = inputSpec.getLong("pollingInterval");
        }
        if (delay == -1L) {
            delay = DEFAULT_RESTART_DELAY;
        }

        mainBuilder.addStatement(
                "env.setRestartStrategy($T.fixedDelayRestart($L, $T.of($L, $T.MILLISECONDS)))",
                RestartStrategies.class,
                DEFAULT_RESTART_ATTEMPTS,
                Time.class,
                delay,
                TimeUnit.class);
    }

    // TODO: Why are we building api config like this instead of directly passing json
    private void inputSpecBuilder(Builder mainBuilder, JSONObject inputSpec,
                                  JSONObject inputParseSpec) {

        if ("http".equals(inputSpec.getString("type"))) {
            mainBuilder
                    .addStatement(
                            "$T apiConfig = new $T().setUrl($S).setRequestType($S).setPollingInterval($L)",
                            ApiConfig.class, ApiConfig.class,
                            inputSpec.getString("url"), inputSpec.getString("requestType"),
                            inputSpec.getLong("pollingInterval"));

            if (inputSpec.has("headers")) {
                JSONArray headers = inputSpec.getJSONArray("headers");
                for (int i = 0; i < headers.length(); i++) {
                    JSONObject header = headers.getJSONObject(i);
                    mainBuilder.addStatement(
                            "apiConfig.setHeader($S, $S)",
                            header.getString("key"), header.getString("value"));
                }
            }

            if (inputSpec.has("requestGenerationScripts")) {
                JSONArray scripts = inputSpec.getJSONArray("requestGenerationScripts");
                for (int i = 0; i < scripts.length(); i++) {
                    JSONObject script = scripts.getJSONObject(i);
                    mainBuilder.addStatement(
                            "apiConfig.setParamGenScript($S, $S, $S)",
                            script.getString("in"), script.getString("pattern"),
                            script.getString("script"));
                }
            }

            if (inputSpec.has("postBody")) {
                mainBuilder.addStatement("apiConfig.setBody($S)", inputSpec.getString("postBody"));
            }

            if (inputSpec.has("requestTimeout")) {
                mainBuilder.addStatement("apiConfig.setRequestTimeout($L)", inputSpec.getLong("requestTimeout"));
            }

            if (inputSpec.has("boundedJob")) {
                JSONObject minioConfigSpec = inputSpec.getJSONObject("minioConfig");

                mainBuilder.addStatement("$T minioConfig = new $T.Builder($S).bucket($S).object($S).credentials($S, $S).build()",
                        MinioConfig.class, MinioConfig.class,
                        minioConfigSpec.getString("url"), minioConfigSpec.getString("bucket"), minioConfigSpec.getString("stateName"),
                        minioConfigSpec.getString("accessKey"), minioConfigSpec.getString(("secretKey")));
            }
        }

        if ("rmq".equals(inputSpec.getString("type"))) {

            mainBuilder.addStatement("$T config = new $T()", RMQConfig.class, RMQConfig.class);
            mainBuilder.addStatement("config.setUri($S)", inputSpec.getString("uri"));
            mainBuilder.addStatement("config.setQueueName($S)", inputSpec.getString("queueName"));

            mainBuilder.addStatement("$T source = new $T<>(config, $T.of($T.class), $S, $S)",
                    RMQGenericSource.class, RMQGenericSource.class, TypeInformation.class,
                    Message.class, tc.name, inputParseSpec);

            mainBuilder.addStatement(
                    "$T<$T> so = env.addSource(source)",
                    DataStreamSource.class, Message.class);
        }
    }

    private void parseSpecBuilder(Builder mainBuilder, JSONObject parseSpec) {

        String messageType = parseSpec.getString("type");
        mainBuilder.addStatement("String parseSpec = $S", parseSpec.toString());

        if ("json".equals(messageType)) {
            String containerType = parseSpec.getString("messageContainer");
            if ("array".equals(containerType)) {
                containerType = "array";
                mainBuilder.addStatement("$T<$T<$T>> parser = new $T<$T<$T>>(parseSpec)",
                        JsonPathParser.class, List.class, Message.class,
                        JsonPathParser.class, List.class, Message.class);
                mainBuilder.addStatement(
                        "$T<$T> so = env.addSource(new $T<$T<$T>>(apiConfig, parser))",
                        DataStreamSource.class, Message.class,
                        HttpSource.class, List.class, Message.class);

            } else if ("single".equals(containerType)) {
                containerType = "single";
                mainBuilder.addStatement("$T<$T> parser = new $T<$T>(parseSpec)",
                        JsonPathParser.class, Message.class,
                        JsonPathParser.class, Message.class);
                mainBuilder.addStatement(
                        "$T<$T> so = env.addSource(new $T<$T>(apiConfig, parser))",
                        DataStreamSource.class, Message.class,
                        HttpSource.class, Message.class);
            }
        }
    }

    private void ruleSourceSpecBuilder(Builder mainBuilder, JSONObject ruleSourceSpec,
                                       JSONObject ruleSourceParseSpec) {
        if ("rmq".equals(ruleSourceSpec.getString("type"))) {

            mainBuilder.addStatement("$T ruleConfig = new $T()", RMQConfig.class, RMQConfig.class);
            mainBuilder.addStatement("ruleConfig.setUri($S)", ruleSourceSpec.getString("uri"));
            mainBuilder.addStatement("ruleConfig.setQueueName($S)", ruleSourceSpec.getString("queueName"));

            mainBuilder.addStatement("$T ruleSource = new $T<>(ruleConfig, $T.of($T.class), $S, $S)",
                    RMQGenericSource.class, RMQGenericSource.class, TypeInformation.class,
                    Rule.class, tc.name, ruleSourceParseSpec);

            mainBuilder.addStatement(
                    "$T<$T> rules = env.addSource(ruleSource)",
                    DataStreamSource.class, Rule.class);
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
            hasGenericTransformer = true;
            mainBuilder.addStatement("$T trans = new $T(transformSpec)",
                    JoltTransformer.class, JoltTransformer.class);
        }
        if ("js".equals(transformType)) {
            hasJSTransformer = true;
            hasGenericTransformer = false;
        }
        if ("jsPath".equals(transformType)) {
            hasJSPathTransformer = true;
            hasGenericTransformer = false;
        }
    }

    private void publishSpecBuilder(Builder mainBuilder, JSONObject publishSpec) {
        String publishType = publishSpec.getString("type");

        if ("rmq".equals(publishType)) {
            mainBuilder.addStatement("$T rmqConfig = new $T()",
                    RMQConfig.class, RMQConfig.class);
            mainBuilder.addStatement("rmqConfig.setUri($S)",
                                          publishSpec.getString("uri"));

            if (publishSpec.has("sinkName")) {
                mainBuilder.addStatement("rmqConfig.setExchange($S)",
                        publishSpec.getString("sinkName"));
            }

            if (publishSpec.has("tagName")) {
                mainBuilder.addStatement("rmqConfig.setRoutingKey($S)",
                        publishSpec.getString("tagName"));
            }

        }
    }

    private void buildTopologyForETL(Builder mainBuilder) {

        /* TODO: Parse and perform
         * TODO: Break this construction logic further
         **/
        if (hasGenericTransformer) {
            if (tc.isBoundedJob) {
                mainBuilder.addStatement("$T<$T> ds = so"
                                + ".keyBy(($T msg) -> msg.key)"
                                + ".process(new $T(trans, dedup, minioConfig))",
                        SingleOutputStreamOperator.class, Message.class,
                        Message.class, BoundedProcessFunction.class);
            } else {
                mainBuilder.addStatement("$T<$T> ds = so"
                                + ".keyBy(($T msg) -> msg.key)"
                                + ".process(new $T(trans, dedup))",
                        SingleOutputStreamOperator.class, Message.class,
                        Message.class, GenericProcessFunction.class);
            }
        } else {
            if (hasJSTransformer) {
                if (tc.isBoundedJob) {
                    mainBuilder.addStatement("$T<$T> ds = so"
                                    + ".keyBy(($T msg) -> msg.key)"
                                    + ".process(new $T(dedup, minioConfig))"
                                    + ".flatMap(new $T(transformSpec))",
                            SingleOutputStreamOperator.class, Message.class,
                            Message.class, BoundedProcessFunction.class,
                            JSProcessFunction.class);
                } else {
                    mainBuilder.addStatement("$T<$T> ds = so"
                                    + ".keyBy(($T msg) -> msg.key)"
                                    + ".process(new $T(dedup))"
                                    + ".flatMap(new $T(transformSpec))",
                            SingleOutputStreamOperator.class, Message.class,
                            Message.class, GenericProcessFunction.class,
                            JSProcessFunction.class);
                }
            }
            if (hasJSPathTransformer) {
                if (tc.isBoundedJob) {
                    mainBuilder.addStatement("$T<$T> ds = so"
                                    + ".keyBy(($T msg) -> msg.key)"
                                    + ".process(new $T(dedup, minioConfig))"
                                    + ".flatMap(new $T(transformSpec))",
                            SingleOutputStreamOperator.class, Message.class,
                            Message.class, BoundedProcessFunction.class,
                            JSPathProcessFunction.class);
                } else {
                    mainBuilder.addStatement("$T<$T> ds = so"
                                    + ".keyBy(($T msg) -> msg.key)"
                                    + ".process(new $T(dedup))"
                                    + ".flatMap(new $T(transformSpec))",
                            SingleOutputStreamOperator.class, Message.class,
                            Message.class, GenericProcessFunction.class,
                            JSPathProcessFunction.class);
                }

            }
        }
        if (tc.isBoundedJob) {
            mainBuilder.addStatement("$T<String> errorStream = ds.getSideOutput($T.errorStream)",
                    DataStream.class, BoundedProcessFunction.class);
        } else {
            mainBuilder.addStatement("$T<String> errorStream = ds.getSideOutput($T.errorStream)",
                    DataStream.class, GenericProcessFunction.class);
        }



        /* TODO: Loki config */

        mainBuilder.addStatement("ds.addSink(new $T<>(rmqConfig, $T.of($T.class)))",
                RMQGenericSink.class, TypeInformation.class, Message.class);

        mainBuilder.beginControlFlow("try");
        mainBuilder.addStatement("env.getConfig().setGlobalJobParameters(parameters)");
        mainBuilder.addStatement("env.execute($S)", tc.name);
        mainBuilder.nextControlFlow("catch (Exception e)");
        mainBuilder.endControlFlow();

    }

    private void buildTopologyForRules(Builder mainBuilder) {

        mainBuilder.addStatement("$T<$T> ruleBroadcastStream = rules.broadcast($T" +
                ".ruleMapStateDescriptor)", BroadcastStream.class, Rule.class,
                RuleStateDescriptor.class);

        mainBuilder.addStatement("$T<$T> ds = so.assignTimestampsAndWatermarks(new $T())" +
                ".keyBy(($T msg) -> msg.key)" +
                ".connect(ruleBroadcastStream)" +
                ".process(new $T())" +
                ".setParallelism(1)",
                SingleOutputStreamOperator.class, RuleResult.class,
                MessageWatermarkStrategy.class, Message.class, RuleFunction.class);

        mainBuilder.addStatement("ds.addSink(new $T<>(rmqConfig, $T.of($T.class)))",
                RMQGenericSink.class, TypeInformation.class, RuleResult.class);

        mainBuilder.beginControlFlow("try");
        mainBuilder.addStatement("env.getConfig().setGlobalJobParameters(parameters)");
        mainBuilder.addStatement("env.execute($S)", tc.name);
        mainBuilder.nextControlFlow("catch (Exception e)");
        mainBuilder.endControlFlow();

    }

}
