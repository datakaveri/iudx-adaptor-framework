package datatypes;

import in.org.iudx.adaptor.datatypes.Message;

import java.sql.Timestamp;
import java.time.Instant;

public class CustomMessage extends Message {
    public Timestamp sqlTimestamp;
    public String ruleString;

    public void setSqlTimestamp(Instant instant) {
        this.sqlTimestamp = Timestamp.from(instant);
    }

    public void setRuleString(String ruleString) {
        this.ruleString = ruleString;
    }
}
