package in.org.iudx.adaptor.datatypes;

import java.io.Serializable;
import java.util.Objects;

public class RuleResult implements Serializable, AdaptorRecord {
  public String result;
  public String sinkExchangeKey;
  public String sinkRoutingKey;

  public RuleResult(String result, String sinkExchangeKey, String sinkRoutingKey) {
    this.result = result;
    this.sinkExchangeKey = sinkExchangeKey;
    this.sinkRoutingKey = sinkRoutingKey;
  }

  public String getResult() {
    return result;
  }

  public String getSinkExchangeKey() {
    return sinkExchangeKey;
  }

  public String getSinkRoutingKey() {
    return sinkRoutingKey;
  }

  @Override
  public String toString() {
    return "RuleResult{" + "result='" + result + '\'' + ", sinkExchangeKey='" + sinkExchangeKey + '\'' + ", sinkRoutingKey='" + sinkRoutingKey + '\'' + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RuleResult that = (RuleResult) o;
    return Objects.equals(result, that.result) && sinkExchangeKey.equals(
            that.sinkExchangeKey) && sinkRoutingKey.equals(that.sinkRoutingKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(result, sinkExchangeKey, sinkRoutingKey);
  }
}
