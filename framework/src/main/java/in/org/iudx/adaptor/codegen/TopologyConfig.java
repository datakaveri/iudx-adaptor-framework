package in.org.iudx.adaptor.codegen;


import org.json.JSONObject;

public class TopologyConfig {

  private JSONObject config;

  public String name;
  public JSONObject inputSpec;
  public JSONObject parseSpec;
  public JSONObject deduplicationSpec;
  public JSONObject transformSpec;
  public JSONObject publishSpec;

  public TopologyConfig(String configString) throws Exception {

    config = new JSONObject(configString);
    name = config.getString("name");
    
    // TODO: Run validations
    inputSpec = config.getJSONObject("inputSpec");
    parseSpec = config.getJSONObject("parseSpec");
    deduplicationSpec = config.getJSONObject("deduplicationSpec");
    transformSpec = config.getJSONObject("transformSpec");
    publishSpec = config.getJSONObject("publishSpec");
  }
  

  // TODO: Add getters so the caller doesn't do json parsing
}
