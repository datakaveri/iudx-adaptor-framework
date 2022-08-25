package in.org.iudx.adaptor.datatypes;

import org.apache.flink.api.common.time.Time;

import java.io.Serializable;

public class Rule implements Serializable {

    public String sqlQuery;
    public Integer ruleId;

    private Integer windowMinutes;

    public Rule(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public Long getWindowMillis() {
        return Time.minutes(this.windowMinutes).toMilliseconds();
    }
}
