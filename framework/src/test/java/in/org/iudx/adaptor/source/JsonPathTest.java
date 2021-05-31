package in.org.iudx.adaptor.source;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.HashMap;

import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

import in.org.iudx.adaptor.datatypes.Message;

public class JsonPathTest {
  



  @Test
  void parseFlat() throws InterruptedException {

    String parseSpecObj = new JSONObject()
      .put("timestampPath", "$.time")
      .put("keyPath", "$.id")
      .put("inputTimeFormat","yyyy-MM-dd HH:mm:ss")
      .put("outputTimeFormat", "yyyy-MM-dd'T'HH:mm:ssXXX")
      .toString();

    String data = new JSONObject()
      .put("time", "2021-04-01 12:00:01")
      .put("id", "123")
      .put("k", 1.5)
      .toString();

    JsonPathParser<Message> parser = new JsonPathParser<Message>(parseSpecObj);
    Message msg = parser.parse(data);
    System.out.println(msg.toString());

  }

  @Test
  void parseArr() throws InterruptedException {

    String parseSpecArr = new JSONObject()
      .put("timestampPath", "$.time")
      .put("keyPath", "$.id")
      .put("containerPath", "$.data")
      .put("inputTimeFormat","yyyy-MM-dd HH:mm:ss")
      .put("outputTimeFormat", "yyyy-MM-dd'T'HH:mm:ssXXX")
      .toString();

    String arrayData = 
      new JSONObject().put("data",
      new JSONArray()
      .put(new JSONObject()
      .put("time", "2021-04-01 12:00:01")
      .put("id", "123")
      .put("k", 1.5)
      )
      .put((new JSONObject()
      .put("time", "2021-04-01 12:00:01")
      .put("id", "4356")
      .put("k", 2.5)
      ))).toString();

    JsonPathParser<List<Message>> parser = new JsonPathParser<List<Message>>(parseSpecArr);

    List<Message> m = parser.parse(arrayData);
    for (int i=0; i<m.size(); i++) {
      System.out.println(m.get(i).toString());
    }

  }


  @Test
  void parseTrickle() throws InterruptedException {

    String parseSpecArr = new JSONObject()
      .put("keyPath", "$.id")
      .put("trickle", new JSONArray()
                        .put(new JSONObject()
                            .put("keyPath", "$.time")
                            .put("keyName", "time")))
      .put("timestampPath", "$.time")
      .put("containerPath", "$.data")
      .put("inputTimeFormat","yyyy-MM-dd HH:mm:ss")
      .put("outputTimeFormat", "yyyy-MM-dd'T'HH:mm:ssXXX")
      .toString();

    String arrayData = 
      new JSONObject().put("time", "2021-04-01 12:00:01")
      .put("data",
      new JSONArray()
      .put(new JSONObject()
      .put("id", "123")
      .put("k", 1.5)
      )
      .put((new JSONObject()
      .put("id", "4356")
      .put("k", 2.5)
      ))).toString();

    JsonPathParser<List<Message>> parser = new JsonPathParser<List<Message>>(parseSpecArr);

    List<Message> m = parser.parse(arrayData);
    System.out.println("Parse done");
    for (int i=0; i<m.size(); i++) {
      System.out.println(m.get(i).toString());
    }

  }

}
