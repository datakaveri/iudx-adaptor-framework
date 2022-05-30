package in.org.iudx.adaptor.logger;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class CustomLogger {

    private static Logger logger;
    private String jobID = null;

    public CustomLogger(Class clazz) {
        logger = LogManager.getLogger(clazz);
    }

    public CustomLogger(Class clazz, String jobID) {
        logger = LogManager.getLogger(clazz);
        this.jobID = jobID;
    }

    public void info(Object message) {
        if (this.jobID != null) {
            logger.info("[{}] - {}", this.jobID, message);
        } else {
            logger.info(message);
        }
    }

    public void info(Object message, Throwable t) {
        if (this.jobID != null) {
            logger.info("[{}] - {}", this.jobID, message, t);
        } else {
            logger.info(message, t);
        }
    }

    public void error(Object message) {
        if (this.jobID != null) {
            logger.error("[{}] - {}", this.jobID, message);
        } else {
            logger.error(message);
        }
    }

    public void error(Object message, Throwable t) {
        if (this.jobID != null) {
            logger.error("[{}] - {}", this.jobID, message, t);
        } else {
            logger.error(message, t);
        }
    }

    public void debug(Object message) {
        if (this.jobID != null) {
            logger.debug("[{}] - {}", this.jobID, message);
        } else {
            logger.debug(message);
        }
    }

    public void debug(Object message, Throwable t) {
        if (this.jobID != null) {
            logger.debug("[{}] - {}", this.jobID, message, t);
        } else {
            logger.debug(message, t);
        }
    }

}
