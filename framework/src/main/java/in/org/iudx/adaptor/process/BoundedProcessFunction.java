package in.org.iudx.adaptor.process;

import in.org.iudx.adaptor.codegen.Deduplicator;
import in.org.iudx.adaptor.codegen.MinioConfig;
import in.org.iudx.adaptor.codegen.Transformer;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.logger.CustomLogger;
import in.org.iudx.adaptor.utils.HashMapState;
import in.org.iudx.adaptor.utils.MinioClientHelper;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

public class BoundedProcessFunction extends KeyedProcessFunction<String, Message, Message> {

    private HashMapState streamState;

    private MinioClientHelper minioClientHelper;

    private final Transformer transformer;
    private final Deduplicator deduplicator;
    private final MinioConfig minioConfig;

    public static final OutputTag<String> errorStream
            = new OutputTag<String>("error") {
    };

    private static final long serialVersionUID = 44L;
    transient CustomLogger logger;

    private transient Counter counter;

    public BoundedProcessFunction(Transformer transformer,
                                  Deduplicator deduplicator, MinioConfig minioConfig) {
        this.transformer = transformer;
        this.deduplicator = deduplicator;
        this.minioConfig = minioConfig;
    }

    public BoundedProcessFunction(Deduplicator deduplicator, MinioConfig minioConfig) {
        this.transformer = null;
        this.deduplicator = deduplicator;
        this.minioConfig = minioConfig;
    }

    @Override
    public void open(Configuration config) throws Exception {
        streamState = new HashMapState();
        minioClientHelper = new MinioClientHelper(minioConfig);

        byte[] result = minioClientHelper.getObject();

        if (result != null && result.length != 0) {
            streamState.deserialize(result);
        }

        ExecutionConfig.GlobalJobParameters parameters = getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        String appName = parameters.toMap().get("appName");
        this.logger = new CustomLogger(BoundedProcessFunction.class, appName);

        this.counter = getRuntimeContext()
                .getMetricGroup()
                .counter("BoundedProcessCounter");
    }

    @Override
    public void processElement(Message msg,
                               Context context, Collector<Message> out) throws Exception {
        logger.debug("[event_key - " + msg.key + "] Processing event");
        Message previousMessage = streamState.getMessage(msg);

        /* Update state with current message if not done */
        if (previousMessage == null) {
            streamState.addMessage(msg);
        } else {
            if (deduplicator.isDuplicate(previousMessage, msg)) {
                return;
            }
        }

        try {
            if (transformer == null) {
                out.collect(msg);
            } else {
                Message transformedMessage = transformer.transform(msg);
                out.collect(transformedMessage);
            }
        } catch (Exception e) {
            /* TODO */
            String tmpl =
                    "{\"streams\": [ { \"stream\": { \"flinkhttp\": \"test-sideoutput\"}, \"values\": [[\""
                            + Long.toString(System.currentTimeMillis() * 1000000) + "\", \"error\"]]}]}";
            context.output(errorStream, tmpl);
            logger.error("Error in process element", e);
        }

        this.counter.inc();
        streamState.addMessage(msg);
    }

    @Override
    public void close() {
        try {
            logger.info("Saving state");
            minioClientHelper.putObject(streamState.serialize());
        } catch (Exception e) {
            logger.error("Error saving the state to minio");
        }
    }
}