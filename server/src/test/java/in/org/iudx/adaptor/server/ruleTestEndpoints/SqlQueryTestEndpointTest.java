package in.org.iudx.adaptor.server.ruleTestEndpoints;

import io.vertx.core.json.JsonArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

class SqlQueryTestEndpointTest {
  private static final Logger LOGGER = LogManager.getLogger(SqlQueryTestEndpointTest.class);

  @Test
  void testParseSpec() throws InterruptedException {
    String query = "select * from TABLE";

    String data = "[{\"name\": {\"value\":" +
                    "\"test\"}, \"index\":[1,2]," +
                    "\"observationDateTime\":\"2022-10-06 06:41:37.0\"}," +
                    "{\"name\": {\"value\":" +
                    "\"test1\"}, \"index\":[3,4]," +
                    "\"observationDateTime\":\"2022-10-06 06:41:37.0\"}]";

    SqlQueryTestEndpoint sqlQueryTestEndpoint = new SqlQueryTestEndpoint();
    try {
      String res = sqlQueryTestEndpoint.runQuery(query, data);
      LOGGER.debug(res);
    } catch (Exception e) {
      LOGGER.debug(e);
    }
  }
}
