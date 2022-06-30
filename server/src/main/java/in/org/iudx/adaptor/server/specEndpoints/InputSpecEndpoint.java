package in.org.iudx.adaptor.server.specEndpoints;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.utils.HttpEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public final class InputSpecEndpoint {

  private static final Logger LOGGER = LogManager.getLogger(InputSpecEndpoint.class);

  public InputSpecEndpoint() {
  }

  public String run(JsonObject spec) {
    String url = spec.getString("url");
    String reqType = spec.getString("requestType");

    ApiConfig apiconf = new ApiConfig().setUrl(url)
                                    .setRequestType(reqType)
                                    .setPollingInterval(1000);

    if (spec.containsKey("headers")) {
      JsonArray headers = spec.getJsonArray("headers");
      for (int i = 0; i < headers.size(); i++) {
        JsonObject header = headers.getJsonObject(i);
            apiconf.setHeader(header.getString("key"), header.getString("value"));
      }
    }



    if (spec.containsKey("requestGenerationScripts")) {
      JsonArray scripts = spec.getJsonArray("requestGenerationScripts");
      for (int i = 0; i < scripts.size(); i++) {
        JsonObject script = scripts.getJsonObject(i);
        apiconf.setParamGenScript(script.getString("in"),
                                script.getString("pattern"),
                                script.getString("script"));
      }
    }

    if (spec.containsKey("postBody")) {
      apiconf.setBody(spec.getString("postBody"));
    }

    if (spec.containsKey("requestTimeout")) {
      apiconf.setRequestTimeout(spec.getLong("requestTimeout"));
    }


    try {
      HttpEntity httpEntity = new HttpEntity(apiconf);
      String smsg = httpEntity.getSerializedMessage();
      if (smsg.isEmpty()) {
        return "NULL";
      }
      return smsg;
    } catch (Exception e) {
      return e.getMessage();
    }
  }
}
