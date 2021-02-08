package in.org.iudx.adaptor.codegen;

import org.json.JSONObject;
import in.org.iudx.adaptor.datatypes.Message;


public class SimpleATestTransformer implements Transformer {

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
