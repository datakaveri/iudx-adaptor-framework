package in.org.iudx.adaptor.logger;

import org.junit.jupiter.api.Test;

class LoggerTest {
    @Test
    void loggerMethodsWithAppName() throws InterruptedException {
        CustomLogger logger = new CustomLogger(LoggerTest.class, "jobID goes here");
        logger.info("It is info message");
        logger.error("It is error message", new Throwable("error message"));
        logger.debug("It is debug message");
    }

    @Test
    void loggerMethodsWithoutAppName() throws InterruptedException {
        CustomLogger logger = new CustomLogger(LoggerTest.class);
        logger.info("It is info message");
        logger.error("It is error message", new Throwable("error message"));
        logger.debug("It is debug message");
    }
}
