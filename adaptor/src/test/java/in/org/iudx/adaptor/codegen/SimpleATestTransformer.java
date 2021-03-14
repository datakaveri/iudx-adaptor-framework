package in.org.iudx.adaptor.codegen;

import org.json.JSONObject;
import in.org.iudx.adaptor.datatypes.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SimpleATestTransformer implements Transformer {

  private static final Logger LOGGER = LogManager.getLogger(SimpleATestTransformer.class);

  public SimpleATestTransformer() {
  }

  public Message transform(Message inMessage) {

    JSONObject obj = new JSONObject(inMessage.body);
    obj.put("id", obj.get("deviceId"));
    obj.remove("deviceId");
    inMessage.setResponseBody(obj.toString());
    return inMessage;
  }

}
