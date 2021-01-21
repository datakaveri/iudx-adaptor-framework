package in.org.iudx.adaptor.codegen;

import org.json.JSONObject;
import java.time.Instant;
import in.org.iudx.adaptor.datatypes.Message;

public class SimpleDeduplicator implements Deduplicator {

  public SimpleDeduplicator() {
  }

  public boolean isDuplicate(Message msg) {
    return false;
  }


}
