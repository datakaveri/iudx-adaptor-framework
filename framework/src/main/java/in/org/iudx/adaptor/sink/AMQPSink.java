package in.org.iudx.adaptor.sink;

import in.org.iudx.adaptor.logger.CustomLogger;
import in.org.iudx.adaptor.process.GenericProcessFunction;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSink;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.codegen.RMQConfig;
import org.apache.flink.api.common.serialization.SerializationSchema;

import in.org.iudx.adaptor.sink.StaticStringPublisher;


/**
 * {@link AMQPSink} - Extends Flink native RMQSink {@link RMQSink}
 * This is a wrapper to extend functionality to RMQSink for posterity.
 */
public class AMQPSink extends RMQSink<Message> {

    private RMQConfig rmqConfig;

    transient CustomLogger logger;
    private transient Counter counter;

    public AMQPSink(RMQConfig rmqConfig) {
        super(rmqConfig.connectionConfig, rmqConfig.publisher, rmqConfig);
        this.rmqConfig = rmqConfig;
    }

    @Override
    public void open(Configuration config) throws Exception {
        super.open(config);

        ExecutionConfig.GlobalJobParameters parameters = getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        String appName = parameters.toMap().get("appName");
        logger = new CustomLogger(GenericProcessFunction.class, appName);

        this.counter = getRuntimeContext()
                .getMetricGroup()
                .counter("RMQSinkCounter");
    }

    @Override
    public void invoke(Message value, Context context) throws Exception {
        try {
            logger.info("Pushing output to sink");
            super.invoke(value, context);
            this.counter.inc();
        } catch (Exception e) {
            logger.error("Failed to publish to sink");
            throw e;
        }

    }
}
