package in.org.iudx.adaptor.codegen;

import org.json.JSONObject;
import java.time.Instant;
import in.org.iudx.adaptor.datatypes.Message;

public class SimpleADeduplicator implements Deduplicator {

  public SimpleADeduplicator() {
  }

  public boolean isDuplicate(Message state, Message msg) {
    if (state.timestamp.equals(msg.timestamp)) {
      return true;
    } else {
      return false;
    }
  }


}
