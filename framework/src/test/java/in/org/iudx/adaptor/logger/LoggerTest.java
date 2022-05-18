package in.org.iudx.adaptor.logger;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;

class LoggerTest {
    @Test
    void loggerInfoMethod() throws InterruptedException {
        CustomLogger logger = (CustomLogger) CustomLogger.getLogger(LoggerTest.class, "jobID goes here");
        logger.info("It is info message");
        logger.error("It is error message");
        logger.debug("It is debug message");
    }
}
