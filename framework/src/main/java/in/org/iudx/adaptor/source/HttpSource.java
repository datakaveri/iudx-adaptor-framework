package in.org.iudx.adaptor.source;

import in.org.iudx.adaptor.logger.CustomLogger;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.metrics.Counter;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.configuration.Configuration;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.codegen.Parser;

import in.org.iudx.adaptor.utils.HttpEntity;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;


/**
 * {@link HttpSource} - The HttpSource Class
 * <p>
 * This extends {@link RichSourceFunction} which implements stateful functionalities.
 * This generic function exchanges meesages as {@link Message} objects.
 * <p>
 * PO - Parser Output
 * <p>
 * Notes:
 * - ?This is serializable from flink examples
 * - The constructor can only take a serializable object, {@link ApiConfig}
 */
public class HttpSource<PO> extends RichSourceFunction<Message> {

    private static final long serialVersionUID = 1L;
    private volatile boolean running = true;
    private HttpEntity httpEntity;
    private ApiConfig apiConfig;
    private Parser<PO> parser;

    private ScriptEngine engine;
    private ScriptContext context;

    transient CustomLogger logger;

    private transient Counter counter;

    private volatile boolean isCanceled = false;

    /**
     * {@link HttpEntity} Constructor
     *
     * @param apiConfig Api configuration object
     *                  <p>
     *                  Note:
     *                  - Only set configuration here. Don't initialize {@link HttpEntity}.
     *                  <p>
     */
    public HttpSource(ApiConfig apiConfig, Parser<PO> parser) {
        this.apiConfig = apiConfig;
        this.parser = parser;
    }

    /**
     * Retrieve stateful context info
     *
     * @param config Flink managed state configuration
     *               <p>
     *               Note:
     *               - This is where {@link HttpEntity} must be initialized
     */
    @Override
    public void open(Configuration config) throws Exception {
        super.open(config);
        ExecutionConfig.GlobalJobParameters parameters = getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        String appName = parameters.toMap().get("appName");
        logger = new CustomLogger(HttpSource.class, appName);
        httpEntity = new HttpEntity(apiConfig, appName);

        this.counter = getRuntimeContext()
                .getMetricGroup()
                .counter("HttpSourceCounter");

        engine = new ScriptEngineManager().getEngineByName("nashorn");
        context = new SimpleScriptContext();
        context.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
    }

    public void emitMessage(SourceContext<Message> ctx) {
        logger.debug("Executing http source");
        this.counter.inc();
        String serializedMessage = httpEntity.getSerializedMessage();
        if (Objects.equals(serializedMessage, "") || serializedMessage.isEmpty()) {
            return;
        }
        try {
            PO msg = parser.parse(serializedMessage);
            /* Message array */
            if (msg instanceof ArrayList) {
                ArrayList<Message> message = (ArrayList<Message>) msg;
                for (int i = 0; i < message.size(); i++) {
                    Message m = (Message) message.get(i);
                    logger.debug("[event_key - " + m.key + "] Emitting event from http source");
                    ctx.collectWithTimestamp(m, m.getEventTime());
                    ctx.emitWatermark(new Watermark(m.getEventTime()));
                }
            }
            /* Single object */
            if (msg instanceof Message) {
                Message m = (Message) msg;
                logger.debug("[event_key - " + m.key + "] Emitting event from http source");
                ctx.collectWithTimestamp(m, m.getEventTime());
                ctx.emitWatermark(new Watermark(m.getEventTime()));
            }
        } catch (Exception e) {
            // Do nothing
            logger.error("[HttpSource] Error emitting source data", e);
        }

    }

    /**
     * Forever loop with a delay
     *
     * @param ctx ?Context
     *            <p>
     *            TODOS:
     *            - Is thread.sleep the best way?
     */
    @Override
    public void run(SourceContext<Message> ctx) throws Exception {
        /* TODO: Better way of figuring out batch jobs */
        if (apiConfig.pollingInterval == -1) {
            logger.info("Calling API");
            makeApi(context);
            emitMessage(ctx);
        } else {
            while (!isCanceled) {
                logger.info("Calling API");
                makeApi(context);
                emitMessage(ctx);
                try {
                    Thread.sleep(apiConfig.pollingInterval);
                } catch (InterruptedException e) {
                    if (isCanceled) {
                        Thread.interrupted();
                        return;
                    }
                }
            }
//            while (running) {
//                logger.info("Calling API");
//                makeApi(context);
//                emitMessage(ctx);
//                Thread.sleep(apiConfig.pollingInterval);
//            }
        }
    }

    private void makeApi(ScriptContext cx) throws ScriptException {
        if (apiConfig.hasScript) {
            for (int i = 0; i < apiConfig.scripts.size(); i++) {
                HashMap<String, String> mp = apiConfig.scripts.get(i);
                if (mp.get("in").equals("url")) {
                    String val = engine.eval(mp.get("script"), cx).toString();
                    httpEntity.setUrl(apiConfig.url.replace(mp.get("pattern"), val));
                }
                if (mp.get("in").equals("body")) {
                    String val = engine.eval(mp.get("script"), cx).toString();

                    httpEntity.setUrl(apiConfig.body.replace(mp.get("pattern"), val));
                }
            }

        }
    }

    @Override
    public void cancel() {
        isCanceled = true;
    }

}
