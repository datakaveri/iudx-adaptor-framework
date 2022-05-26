package in.org.iudx.adaptor.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.HashMap;

import java.util.stream.Stream;
import java.util.concurrent.SubmissionPublisher;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.codegen.SimpleATestParser;

public class Streamer {

    public Streamer(EndSubscriber<Message> sub) {


        SimpleATestParser parser = new SimpleATestParser();

        ApiConfig apiConfig =
                new ApiConfig().setUrl("http://127.0.0.1:8888/simpleA")
                        .setRequestType("GET")
                        .setPollingInterval(1000L);

        HttpEntity httpEntity = new HttpEntity(apiConfig, "unit_test");

        SubmissionPublisher<Message> pub = new SubmissionPublisher<Message>();
        pub.subscribe(sub);

        for (int i = 0; i < 10; i++) {
            Message msg = parser.parse(httpEntity.getSerializedMessage());
            pub.submit(msg);
        }
        pub.close();

    }
}


