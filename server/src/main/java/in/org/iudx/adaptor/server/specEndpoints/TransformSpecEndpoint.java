package in.org.iudx.adaptor.server.specEndpoints;

import in.org.iudx.adaptor.datatypes.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.org.iudx.adaptor.utils.JSPathProcess;

public class TransformSpecEndpoint {
    public static final Logger LOGGER = LogManager.getLogger(TransformSpecEndpoint.class);


    public String msg;
    public Message message = new Message();
    private String transformSpec;

    private  JsonObject transformSpecJson;

    public TransformSpecEndpoint(JsonObject transformSpec) {
        this.transformSpec = transformSpec.toString();
        this.transformSpecJson = transformSpec;
    }

    public String run(String inputData) {
        String specType = this.transformSpecJson.getString("type");
        message.setResponseBody(inputData);
        switch (specType) {
            case "js":
                try {

                } catch (Exception e) {
                    return e.getMessage();
                }
                break;
            case "jsPath":
                try {
                    JSPathProcess jsPathProcess = new JSPathProcess(this.transformSpec);

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
