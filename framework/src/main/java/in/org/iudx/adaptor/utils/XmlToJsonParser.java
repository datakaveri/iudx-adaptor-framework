package in.org.iudx.adaptor.utils;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@lombok.Data
class Data {
  private Long id;
  private String name;
}

public class XmlToJsonParser {
  @Test
  public void xmlToJson() throws IOException {
    String xml = "<Data><id>1</id><name>John Doe</name></Data>";

    XmlMapper xmlMapper = new XmlMapper();
    Data dataInstance = xmlMapper.readValue(xml.getBytes(), Data.class);

    JsonMapper jsonMapper = new JsonMapper();
    String json = jsonMapper.writeValueAsString(dataInstance);

    Assertions.assertEquals("{\"id\":1,\"name\":\"John Doe\"}", json);
  }
}
