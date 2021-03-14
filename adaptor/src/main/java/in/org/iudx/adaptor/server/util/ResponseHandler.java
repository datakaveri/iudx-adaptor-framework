package in.org.iudx.adaptor.server.util;

import static in.org.iudx.adaptor.server.util.Constants.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ResponseHandler {

  private final String status;
  private JsonArray results = new JsonArray();

  /**
   * Handles the response format to manage Uniformity of the response body.
   * 
   * @param status is a string based error status like: invalidValue or invalidSyntax
   * @param results is a JsonArray, may contains other response related values
   */
  public ResponseHandler(String status, JsonArray results) {
    super();
    this.status = status;
    this.results = results;
  }

  /**
   * Creates a JsonObject of status and results.
   * 
   * @return json of status and results
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.put("status", status);
    json.put("results", results);
    return json;
  }

  public String toJsonString() {
    return toJson().toString();
  }

  public static class Builder {
    private String status;
    private JsonArray results = new JsonArray();

    public Builder() {}

    public Builder withStatus(String status) {
      this.status = status;
      return this;
    }

    /**
     * Builds the results JsonArray.
     * 
     * @param results is a JsonArrays, may contains other response related values
     * @return all properties
     */
    public Builder withResults(JsonArray results) {
      if (results == null) {
        this.results = new JsonArray();
      } else {
        this.results = results;
      }
      return this;
    }

    /**
     * 
     * @param id of the item
     * @param method of the request, insert/update/delete
     * @param status of the request, whether success/failed
     * @return
     */
    public Builder withResults(String id, String method, String status) {

      JsonObject resultAttrs = new JsonObject().put(ID, id).put(METHOD, method).put(STATUS, status);
      results.add(resultAttrs);
      return this;
    }

    /**
     * 
     * @param id of the item
     * @param method of the request, insert/update/delete
     * @param status of the request, whether success/failed
     * @param desc description messages of the failed error request
     * @return
     */
    public Builder withResults(String id, String method, String status, String desc) {

      JsonObject resultAttrs =
          new JsonObject().put(ID, id).put(METHOD, method).put(STATUS, status).put(DESC, desc);
      results.add(resultAttrs);
      return this;
    }

    public ResponseHandler build() {
      return new ResponseHandler(status, results);
    }

  }

}
