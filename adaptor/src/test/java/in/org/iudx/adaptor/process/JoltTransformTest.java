package in.org.iudx.adaptor.process;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import in.org.iudx.adaptor.datatypes.Message;

import java.util.Map;
import java.util.HashMap;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;

import java.io.IOException;
import java.util.List;



public class JoltTransformTest {

  @Test
  void transform() throws InterruptedException {

    // How to access the test artifacts, i.e. JSON files
    //  JsonUtils.classpathToList : assumes you put the test artifacts in your class path
    //  JsonUtils.filepathToList : you can use an absolute path to specify the files
    //  



    String joltSpec = "[{ \"operation\": \"shift\", \"spec\": { \"time\": \"observationDateTime\", \"deviceId\": \"id\", \"k1\": \"k1\" } }, { \"operation\": \"modify-overwrite-beta\", \"spec\": { \"id\": \"=concat('datakaveri.org/123/', id)\" } }]";


    String transformSpec = new JSONObject().put("transformType", "jolt")
                                                .put("joltSpec", joltSpec)
                                                .toString();

    String input = "{ \"time\": \"2021-03-11T12:59:20Z\", \"k1\": 769, \"deviceId\": \"abc-123\" }";

    Message msg = new Message().setResponseBody(new JSONObject(input).toString());
                                  
    JoltTransformer trans = new JoltTransformer(transformSpec);


    try {
      System.out.println(trans.transform(msg));
    } catch (Exception e) {
      System.out.println(e);
    }

  }
}
