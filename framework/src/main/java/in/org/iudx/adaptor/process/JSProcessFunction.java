package in.org.iudx.adaptor.process;

import in.org.iudx.adaptor.logger.CustomLogger;
import in.org.iudx.adaptor.utils.JSProcess;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.metrics.Counter;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import in.org.iudx.adaptor.datatypes.Message;



/* NOTE: Can't apply Transformer interface here because of serialization requirement */

public class JSProcessFunction extends RichFlatMapFunction<Message, Message> {

    /* Something temporary for now */
    private String STATE_NAME = "api state";

    transient JSProcess jsProcess;

    String transformSpec;

    public static final OutputTag<String> errorStream = new OutputTag<String>("error") {
    };

    transient CustomLogger logger;

    private static final long serialVersionUID = 49L;

    private transient Counter counterIn;
    private transient Counter counterOut;

    public JSProcessFunction(String transformSpec) {
        this.transformSpec = transformSpec;
    }

    @Override
    public void open(Configuration config) {
        ExecutionConfig.GlobalJobParameters parameters = getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        String appName = parameters.toMap().get("appName");
        logger = new CustomLogger(JSPathProcessFunction.class, appName);

        this.counterIn = getRuntimeContext()
                .getMetricGroup()
                .counter("JSProcessCounterIn");

        this.counterOut = getRuntimeContext()
                .getMetricGroup()
                .counter("JSProcessCounterOut");
        try {
            this.jsProcess = new JSProcess(this.transformSpec);
            this.jsProcess.initializeScriptEngine();
        } catch (Exception e) {
            logger.error("Failed to init script");
        }

    }

    @Override
    public void flatMap(Message msg, Collector<Message> out) throws Exception {
        try {
            this.counterIn.inc();
            String resp = this.jsProcess.process(msg);
            msg.setResponseBody(resp);
            out.collect(msg);
            this.counterOut.inc();
        } catch (Exception e) {
            logger.error("Failed to execute script", e);
        }
    }

}
