package in.org.iudx.adaptor.server.specEndpoints;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.utils.HttpEntity;

public class InputSpecEndpointTest {

    private static final Logger LOGGER = LogManager.getLogger(InputSpecEndpointTest.class);

    @BeforeAll
    public static void intialize() {
    }

    @Test
    void testInputSpec() throws InterruptedException {
        JsonObject spec = new JsonObject();
        spec.put("type", "http")
                .put("url", "http://127.0.0.1:8888/simpleA")
                .put("requestType", "GET")
                .put("pollingInterval", 1000L)
                .put("requestTimeout", 20L);

        InputSpecEndpoint ise = new InputSpecEndpoint();
        String res = ise.run(spec);
        LOGGER.debug(res);
    }

    @Test
    void testHttpEntity() throws InterruptedException {
        ApiConfig apiConfig =
                new ApiConfig().setUrl("http://127.0.0.1:8888/simpleA")
                        .setRequestType("GET")
                        .setPollingInterval(1000L);

        HttpEntity httpEntity = new HttpEntity(apiConfig);
        LOGGER.info("SimpleGet :" + httpEntity.getSerializedMessage());
    }

}
