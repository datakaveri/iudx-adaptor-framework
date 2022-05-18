package in.org.iudx.adaptor.logger;

import in.org.iudx.adaptor.logger.CustomLogger;
import org.junit.jupiter.api.Test;

public class LoggerTest {

    @Test
    void loggerInfoMethod() throws InterruptedException {
        CustomLogger logger = (CustomLogger) CustomLogger.getLogger("LoggerTest");
        logger.info("It works");
    }
}
