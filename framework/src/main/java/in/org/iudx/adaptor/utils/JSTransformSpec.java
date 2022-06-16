package in.org.iudx.adaptor.utils;

import java.io.Serializable;



/**
 * {@link ApiConfig} - Api configuration class
 * Encapsulates api information such as making a url,
 * defining request type, adding headers etc.
 * Note: This must be serializable since flink passes
 * this accross its instances.
 *
 * TODO: 
 *  - Extend as needed
 *
 */
public class JSTransformSpec implements Serializable {

  private static final long serialVersionUID = 29L;
  public String script;

  public JSTransformSpec() {
  }

  public JSTransformSpec setScript(String script) {
    this.script = script;
    return this;
  }

  public String getScript() {
    return script;
  }
}
