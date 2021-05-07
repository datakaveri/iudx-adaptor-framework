package in.org.iudx.adaptor.process;

import java.io.Serializable;
import org.json.JSONObject;
import org.json.JSONArray;



/**
 * TODO: 
 *  - Validations
 */
public class JSPathSpec implements Serializable {

  private static final long serialVersionUID = 29L;
  private String pathSpec;
  private String template;

  public JSPathSpec() {
  }

  public JSPathSpec setPathSpec(String template, String pathSpec) {
    this.pathSpec = pathSpec;
    this.template = template;
    return this;
  }

  public String getPathSpec() {
    return pathSpec;
  }

  public String getTemplate() {
    return template;
  }
}
