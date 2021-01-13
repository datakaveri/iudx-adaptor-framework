package in.org.iudx.adaptor.codegen;

import org.json.JSONObject;
import java.time.Instant;

public class SimpleTestTagger implements Tagger {

  public SimpleTestTagger() {
  }

  public String getKey(JSONObject data) {
    return data.getString("deviceId");
  }

  public Instant getTimeIndex(JSONObject data) {
    return Instant.parse(data.getString("time"));
  }
}
