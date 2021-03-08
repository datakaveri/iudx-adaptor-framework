package in.org.iudx.adaptor.testadaptors;

import org.json.JSONObject;
import java.time.Instant;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.codegen.Deduplicator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleADeduplicator implements Deduplicator {

  private static final Logger LOGGER = LogManager.getLogger(SimpleADeduplicator.class);

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
