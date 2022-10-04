package in.org.iudx.adaptor.codegen;


import org.json.JSONObject;

public class TopologyConfig {

  private JSONObject config;

  public String name;
  public JSONObject failureRecoverySpec;
  public JSONObject inputSpec;
  public JSONObject parseSpec;
  public JSONObject deduplicationSpec;
  public JSONObject transformSpec;
  public JSONObject publishSpec;
  public boolean hasFailureRecovery;
  public boolean isBoundedJob;
  public long pollingInterval;

  public AdaptorType adaptorType;
  public JSONObject ruleSourceSpec;
  public JSONObject ruleSourceParseSpec;
  public JSONObject inputSourceParseSpec;

  public boolean hasTransformSpec;
  public boolean hasDedupSpec;

  public TopologyConfig(String configString) throws Exception {

    config = new JSONObject(configString);
    name = config.getString("name");

    hasFailureRecovery = false;
    isBoundedJob = false;

    if (config.has("adaptorType")) {
      adaptorType = config.getEnum(AdaptorType.class, "adaptorType");
    } else {
      adaptorType = AdaptorType.ETL;
    }
    
    // TODO: Run validations
    if (config.has("failureRecoverySpec")) {
      failureRecoverySpec = config.getJSONObject("failureRecoverySpec");
      hasFailureRecovery = true;
    }
    inputSpec = config.getJSONObject("inputSpec");

    if (adaptorType == AdaptorType.ETL) {
      parseSpec = config.getJSONObject("parseSpec");
    }

    if (config.has("transformSpec")) {
      transformSpec = config.getJSONObject("transformSpec");
      hasTransformSpec = true;
    }

    if (config.has("deduplicationSpec")) {
      deduplicationSpec = config.getJSONObject("deduplicationSpec");
      hasDedupSpec = true;
    }

    publishSpec = config.getJSONObject("publishSpec");
    if (inputSpec.has("boundedJob") && inputSpec.getBoolean("boundedJob")) {
      isBoundedJob = true;
    }

    if (inputSpec.has("pollingInterval")) {
      pollingInterval = inputSpec.getLong("pollingInterval");
    }

    if (adaptorType.equals(AdaptorType.RULES)) {
      if (config.has("ruleSourceSpec")) {
        ruleSourceSpec = config.getJSONObject("ruleSourceSpec");
      }

      if (ruleSourceSpec.has("parseSpec")) {
        ruleSourceParseSpec = ruleSourceSpec.getJSONObject("parseSpec");
      }

      if (inputSpec.has("parseSpec")) {
        inputSourceParseSpec = inputSpec.getJSONObject("parseSpec");
      }
    }
  }
  

  // TODO: Add getters so the caller doesn't do json parsing

  public enum AdaptorType {
    ETL, RULES
  }
}
