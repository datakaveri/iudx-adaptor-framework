package in.org.iudx.adaptor.utils;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.io.IOException;

@lombok.Data
class Data {
  private Long id;
  private String name;
}

class Traveler {
  private Integer id;
  private String name;
  private String email;
  private String adderes;
  private String createdat;
}

public class XmlToJsonParser {
//  @Test
  public void xmlToJson() throws IOException {
    XmlMapper mapper = new XmlMapper();

    try(CloseableHttpClient client = HttpClients.createDefault()) {
      HttpGet request = new HttpGet("http://restapi.adequateshop.com/api/Traveler/11133");

      Traveler response = client.execute(request, httpResponse -> mapper.readValue(httpResponse.getEntity().getContent(), Traveler.class));

      JsonMapper jsonMapper = new JsonMapper();
      String json = jsonMapper.writeValueAsString(response);
      System.out.println(json);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }
}
