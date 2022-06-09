package in.org.iudx.adaptor.process;

import in.org.iudx.adaptor.logger.CustomLogger;
import in.org.iudx.adaptor.utils.JSPathProcess;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import in.org.iudx.adaptor.datatypes.Message;

import java.util.Objects;

/* NOTE: Can't apply Transformer interface here because of serialization requirement */

public class JSPathProcessFunction extends RichFlatMapFunction<Message, Message> {

    /* Something temporary for now */
    private String STATE_NAME = "api state";

    public static final OutputTag<String> errorStream = new OutputTag<String>("error") {
    };

    transient CustomLogger logger;

    private static final long serialVersionUID = 49L;

    transient JSPathProcess jsPathProcess;
    private String pathSpec;

    public JSPathProcessFunction(String pathSpec) {
        this.pathSpec = pathSpec;
    }

    @Override
    public void open(Configuration config) {
        ExecutionConfig.GlobalJobParameters parameters = getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        String appName = parameters.toMap().get("appName");
        logger = new CustomLogger(JSPathProcessFunction.class, appName);
        this.jsPathProcess = new JSPathProcess(this.pathSpec);
    }

    @Override
    public void flatMap(Message msg, Collector<Message> out) throws Exception {
        /* Update state with current message if not done */

        logger.info("Transforming data");
        try {
            String output = this.jsPathProcess.process(msg);
            if (!Objects.equals(output, "")) {
                msg.setResponseBody(output);
                out.collect(msg);
            } else {
                logger.error("No data found while processing");
            }

        } catch (Exception e) {
            logger.error("Failed to process element", e);
            throw e;
        }

    }

}
