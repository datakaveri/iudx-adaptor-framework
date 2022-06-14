package in.org.iudx.adaptor.server.specEndpoints;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.source.JsonPathParser;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.org.iudx.adaptor.utils.JSPathProcess;
import in.org.iudx.adaptor.utils.JSProcess;

public class TransformSpecEndpoint {
    public static final Logger LOGGER = LogManager.getLogger(TransformSpecEndpoint.class);

    private JsonPathParser jsonPathParser;
    private JSPathProcess jsPathProcess;
    private JSProcess jsProcess;

    public String msg;

    public TransformSpecEndpoint() {

    }

    public String run(String inputData, JsonObject spec) {
        String messageContainer = spec.getString("type");
        if(messageContainer.equals("js")) {
//            jsProcess = new JSProcess(spec.toString());
            jsonPathParser = new JsonPathParser<Message>(spec.toString());
            try {
                msg = jsProcess.process(jsonPathParser);
            } catch (Exception e) {
                return e.getMessage();
            }
        } else if (messageContainer.equals("jsPath")) {
            jsPathProcess = new JSPathProcess(spec.toString());
            try {
                msg = jsPathProcess.process(spec).toString();
            } catch (Exception e) {
               return e.getMessage();
            }
        } else if (messageContainer.equals("jolt")) {
            // TODO: Response for Jolt spec
        }
        return msg;
    }
}
