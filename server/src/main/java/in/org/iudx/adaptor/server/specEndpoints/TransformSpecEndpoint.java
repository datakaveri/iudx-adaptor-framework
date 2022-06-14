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

    private JSPathProcess jsPathProcess;
    private JSProcess jsProcess;

    public String msg;
    public Message message = new Message();


    public TransformSpecEndpoint() {

    }

    public String run(String inputData, JsonObject spec) {
        String specType = spec.getString("type");
        message.setResponseBody(inputData);
        switch (specType) {
            case "js":
                try {
                    msg = jsProcess.process(message);
                } catch (Exception e) {
                    return e.getMessage();
                }
                break;
            case "jsPath":
                try {
                    msg = jsPathProcess.process(message);
                } catch (Exception e) {
                    return e.getMessage();
                }
                break;
            case "jolt":
                // TODO: Response for Jolt spec
                break;
            default:
                return "Invalid argument provided. Supported types are 'js', 'jsPath' & 'jolt'";
        }
        return msg;
    }
}
