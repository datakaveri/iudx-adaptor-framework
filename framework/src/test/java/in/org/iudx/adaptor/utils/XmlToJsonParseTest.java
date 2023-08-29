package in.org.iudx.adaptor.utils;

import in.org.iudx.adaptor.codegen.ApiConfig;
import org.apache.hc.core5.http.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

public class XmlToJsonParseTest {
  private static final Logger LOGGER = LogManager.getLogger(XmlToJsonParseTest.class);

  @Test
   void parseXmlToJson() {
    ApiConfig apiConfig = new ApiConfig().setUrl("http://restapi.adequateshop.com/api/Traveler/11133")
      .setResponseType(ContentType.APPLICATION_XML)
      .setPollingInterval(1000L)
      .setRequestType("GET");

    HttpEntity httpEntity = new HttpEntity(apiConfig, "unit_test");
    String res  = httpEntity.getSerializedMessage();
    LOGGER.info(res);
  }
}
