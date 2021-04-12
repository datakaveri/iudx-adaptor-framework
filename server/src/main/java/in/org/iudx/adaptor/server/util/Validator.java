package in.org.iudx.adaptor.server.util;

import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

public class Validator {

  private final JsonSchema schema;

  /**
   * Initializes the Json validation object.
   * 
   * @param schemaPath
   * @throws IOException
   * @throws ProcessingException
   */
  public Validator(String schemaPath) throws IOException, ProcessingException {
    final JsonNode schemaNode = JsonLoader.fromResource(schemaPath);
    final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
    schema = factory.getJsonSchema(schemaNode);
  }

  /**
   * Check validity of json encoded string.
   *
   * @param obj Json encoded string object
   * @return isValid boolean
   */
  public boolean validate(String obj) {
    boolean isValid;
    try {
      JsonNode jsonobj = JsonLoader.fromString(obj);
      isValid = schema.validInstance(jsonobj);
    } catch (IOException | ProcessingException e) {
      isValid = false;
    }
    return isValid;
  }
}
