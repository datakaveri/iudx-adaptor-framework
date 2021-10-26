package in.org.iudx.adaptor.process;

import in.org.iudx.adaptor.codegen.Deduplicator;
import in.org.iudx.adaptor.codegen.MinioConfig;
import in.org.iudx.adaptor.codegen.Transformer;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.utils.HashMapState;
import in.org.iudx.adaptor.utils.MinioClientHelper;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BoundedProcessFunction extends KeyedProcessFunction<String,Message,Message> {

    private HashMapState streamState;

    private MinioClientHelper minioClientHelper;

    private final Transformer transformer;
    private final Deduplicator deduplicator;
    private final MinioConfig minioConfig;

    public static final OutputTag<String> errorStream
            = new OutputTag<String>("error") {};

    private static final long serialVersionUID = 44L;
    private static final Logger LOGGER = LogManager.getLogger(BoundedProcessFunction.class);

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

        if(result != null && result.length != 0) {
            streamState.deserialize(result);
        }
    }

    @Override
    public void processElement(Message msg,
                               Context context, Collector<Message> out) throws Exception {
        Message previousMessage = streamState.getMessage(msg);

        /* Update state with current message if not done */
        if (previousMessage == null) {
            streamState.addMessage(msg);
        } else {
            if(deduplicator.isDuplicate(previousMessage, msg)) {
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
            context.output(errorStream, tmpl) ;
        }

        streamState.addMessage(msg);
    }

    @Override
    public void close() {
        try{
            minioClientHelper.putObject(streamState.serialize());
        }
        catch(Exception e) {
            LOGGER.error("Error saving the state to minio");
        }
    }
}