package in.org.iudx.adaptor.datatypes;

import org.apache.flink.api.common.time.Time;

import java.io.Serializable;
import java.util.Objects;

public class Rule implements Serializable {

  public Integer ruleId;
  public String sqlQuery;
  public String resultColumnName;
  public RuleType type;
  public String sinkExchangeKey;
  public String sinkRoutingKey;
  private Integer windowMinutes;

  public Rule() {
  }

  public Rule(Integer ruleId, String sqlQuery, String resultColumnName, RuleType type,
              Integer windowMinutes, String sinkExchangeKey, String sinkRoutingKey) {
    this.ruleId = ruleId;
    this.sqlQuery = sqlQuery;
    this.resultColumnName = resultColumnName;
    this.type = type;
    this.windowMinutes = windowMinutes;
    this.sinkExchangeKey = sinkExchangeKey;
    this.sinkRoutingKey = sinkRoutingKey;
  }

  public Integer getRuleId() {
    return ruleId;
  }

  public void setRuleId(Integer ruleId) {
    this.ruleId = ruleId;
  }

  public String getSqlQuery() {
    return sqlQuery;
  }

  public void setSqlQuery(String sqlQuery) {
    this.sqlQuery = sqlQuery;
  }

  public String getResultColumnName() {
    return resultColumnName;
  }

  public void setResultColumnName(String resultColumnName) {
    this.resultColumnName = resultColumnName;
  }

  public RuleType getType() {
    return type;
  }

  public void setType(RuleType type) {
    this.type = type;
  }

  public void setWindowMinutes(Integer windowMinutes) {
    this.windowMinutes = windowMinutes;
  }

  public String getSinkExchangeKey() {
    return sinkExchangeKey;
  }

  public void setSinkExchangeKey(String sinkExchangeKey) {
    this.sinkExchangeKey = sinkExchangeKey;
  }

  public String getSinkRoutingKey() {
    return sinkRoutingKey;
  }

  public void setSinkRoutingKey(String sinkRoutingKey) {
    this.sinkRoutingKey = sinkRoutingKey;
  }

  public Long getWindowMillis() {
    return Time.minutes(this.windowMinutes).toMilliseconds();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Rule rule = (Rule) o;
    return ruleId.equals(rule.ruleId) && Objects.equals(sqlQuery, rule.sqlQuery) && Objects.equals(resultColumnName, rule.resultColumnName) && type == rule.type && Objects.equals(windowMinutes, rule.windowMinutes) && Objects.equals(sinkExchangeKey, rule.sinkExchangeKey) && Objects.equals(sinkRoutingKey, rule.sinkRoutingKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ruleId, sqlQuery, resultColumnName, type, windowMinutes, sinkExchangeKey,
            sinkRoutingKey);
  }

  @Override
  public String toString() {
    return "Rule{" + "ruleId=" + ruleId + ", sqlQuery='" + sqlQuery + '\'' + ", resultColumnName" + "='" + resultColumnName + '\'' + ", type=" + type + ", windowMinutes=" + windowMinutes + ", sinkExchangeKey='" + sinkExchangeKey + '\'' + ", sinkRoutingKey='" + sinkRoutingKey + '\'' + '}';
  }

  public enum RuleType {
    RULE, DELETE
  }
}
