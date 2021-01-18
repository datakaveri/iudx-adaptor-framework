package in.org.iudx.adaptor.codegen;

import java.io.Serializable;
import org.json.JSONObject;
import java.time.Instant;

public interface Parser extends Serializable {
  public String getKey(JSONObject data);
  public Instant getTimeIndex(JSONObject data);
}
