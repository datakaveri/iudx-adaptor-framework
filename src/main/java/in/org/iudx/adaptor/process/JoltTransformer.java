package in.org.iudx.adaptor.process;

import java.util.Map;
import java.util.HashMap;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;

import java.io.IOException;
import java.util.List;
import org.json.JSONObject;

import in.org.iudx.adaptor.codegen.Transformer;
import in.org.iudx.adaptor.datatypes.Message;


/* TODO: 
 *  - Validations
 *  - Exception handling
 */

public class JoltTransformer implements Transformer {

  private String transformSpec;

  public JoltTransformer(String transformSpec) {
    this.transformSpec = new JSONObject(transformSpec).getString("joltSpec");
  }



  public Message transform(Message inMessage) throws Exception {

    System.out.println("Started");
    List chainrSpecJSON = JsonUtils.jsonToList(transformSpec);
    Chainr chainr = Chainr.fromSpec( chainrSpecJSON );

    System.out.println("Here");

    Object inputJSON = JsonUtils.jsonToObject(inMessage.body);
    Object transformedOutput = chainr.transform( inputJSON );

    inMessage.setResponseBody(JsonUtils.toJsonString(transformedOutput));
    return inMessage;
  }
}
