package in.org.iudx.adaptor.codegen;

import org.json.JSONObject;


public class SimpleTestTransformer implements Transformer {

  public SimpleTestTransformer() {
  }

  public String transform(String inDoc) {
    JSONObject obj = new JSONObject(inDoc);
    return obj.toString();
  }

}
