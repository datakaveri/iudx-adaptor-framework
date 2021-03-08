package in.org.iudx.adaptor.testadaptors;

import org.json.JSONObject;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.codegen.Transformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.time.LocalDateTime;
import java.time.ZoneOffset;


public class SimpleATestTransformer implements Transformer {

  private static final Logger LOGGER = LogManager.getLogger(SimpleATestTransformer.class);

  private static String iudxid = "datakaveri.org/abc123/varanasiAqm/";
  public SimpleATestTransformer() {
  }

  public float parseString(String number) {
    return Float.parseFloat(number.split(" ")[0]);
  }

  public Message transform(Message inMessage) {
    JSONObject obj = new JSONObject(inMessage.body);
    JSONObject tmpObj = new JSONObject();
    tmpObj.put("id", iudxid + obj.get("EqpName"));
    tmpObj.put("co2", new JSONObject()
                           .put("instValue", parseString(obj.getString("CO2"))));
    tmpObj.put("co", new JSONObject()
                           .put("instValue", parseString(obj.getString("CO"))));
    tmpObj.put("observationDateTime", inMessage.getEventTimeAsString());

    inMessage.setResponseBody(tmpObj.toString());
    return inMessage;
  }

}
