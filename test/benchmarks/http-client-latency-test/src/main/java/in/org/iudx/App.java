package in.org.iudx;

import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.utils.HttpEntity;
import in.org.iudx.adaptor.utils.HttpEntity_DEPRECATED;

public class App {
    static ApiConfig apiConfig =
            new ApiConfig().setUrl("http://13.232.120.105:30002/simpleA")
                    .setRequestType("GET")
                    .setPollingInterval(1000L);

    static long testApacheClient() {
        long startTime = System.currentTimeMillis();
        HttpEntity httpEntity = new HttpEntity(apiConfig, "unit_test");
        httpEntity.getSerializedMessage();
        long stopTime = System.currentTimeMillis();
        return (stopTime - startTime);
    }

    static long testNativeClient() {
        long startTime = System.currentTimeMillis();
        HttpEntity_DEPRECATED httpEntity_old = new HttpEntity_DEPRECATED(apiConfig, "unit_test");
        httpEntity_old.getSerializedMessage();
        long stopTime = System.currentTimeMillis();
        return (stopTime - startTime);
    }

    public static void main(String[] args) {
        int NUMBER_OF_TIMES = 100;
        long nativeClientTotalTIme = 0;
        long apacheClientTotalTime = 0;

        for (int i = 0; i < NUMBER_OF_TIMES; i++) {
            nativeClientTotalTIme += testNativeClient();
        }

        for (int i = 0; i < NUMBER_OF_TIMES; i++) {
            apacheClientTotalTime += testApacheClient();
        }

        System.out.println("Native Http Client Running time :: Time took to run == " + nativeClientTotalTIme / NUMBER_OF_TIMES);
        System.out.println("Apache Http Client Running time :: Time took to run == " + apacheClientTotalTime / NUMBER_OF_TIMES);
    }
}
