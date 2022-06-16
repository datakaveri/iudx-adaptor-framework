package in.org.iudx.adaptor.server.specEndpoints;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class TransformSpecEndpointTest {
    public static final Logger LOGGER = LogManager.getLogger(TransformSpecEndpoint.class);

    @BeforeAll
    public static void intialize() {
    }

    @Test
    void testTransformSpec() throws InterruptedException {
        JsonObject spec = new JsonObject()
                .put("type", "jsPath")
                .put("template",  "{ 'observationDateTime': '2021', 'co2': { 'avgOverTime': 100}, 'id': 'abc'}")
                .put("jsonPathSpec", new JsonArray()
                        .add(new JsonObject()
                                .put("outputKeyPath", "$.observationDateTime")
                                .put("inputValuePath", "$.time")
                        )
                        .add(new JsonObject()
                                .put("outputKeyPath", "$.co2.avgOverTime")
                                .put("inputValuePath", "$.k1")
                        )
                        .add(new JsonObject()
                                .put("outputKeyPath", "$.name")
                                .put("inputValuePath", "$.k2")
                                .put("regexFilter", "^(?!.*reject).*")
                        )
                        .add(new JsonObject()
                                .put("outputKeyPath", "$.id")
                                .put("inputValuePath", "$.deviceId")
                                .put("valueModifierScript", "value.split('-')[0]")
                        )
                );

        String inputData = new JsonObject().put("inputData", new JsonArray()
                .add(new JsonObject()
                        .put("id", "123")
                        .put("k", 1.5)
                        .put("time", "2021-04-01T12:00:01+05:30")
                )
                .add(new JsonObject()
                        .put("id", "4356")
                        .put("k", 2.5)
                        .put("time", "2021-04-01T12:00:01+05:30")
                )).toString();

        TransformSpecEndpoint tse = new TransformSpecEndpoint(spec);
        try {
            String res = tse.run(inputData);
            LOGGER.debug(res);
        } catch(Exception e) {
            LOGGER.debug(e);
        }
    }
}
