package in.org.iudx.adaptor.datatypes;

import org.apache.flink.api.common.time.Time;

import java.io.Serializable;

public class Rule implements Serializable {

    public String sqlQuery;
    public Integer ruleId;
    //TODO: SinkConfig here
    public String sinkInfo;

    private Integer windowMinutes;

    public Rule(String sqlQuery, String sinkInfo) {
        this.sqlQuery = sqlQuery;
        this.sinkInfo = sinkInfo;
    }

    public Long getWindowMillis() {
        return Time.minutes(this.windowMinutes).toMilliseconds();
    }
}
