package in.org.iudx.adaptor.logger;

import org.apache.log4j.*;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.ExtendedLoggerWrapper;


public class CustomLogger extends Logger {

    static String FQCN = CustomLogger.class.getName() + ".";
    static String jobID;

    public CustomLogger(String name, String jobID) {
        super(name);
        this.jobID = jobID;
    }


    public static Logger getLogger(Class name, String jobID) {
        return Logger.getLogger(name.getName(), new CustomLoggerFactory(jobID));
    }

    public void info(Object message) {
        super.log(FQCN, Level.INFO, this.jobID + " - " + message, null);
    }

    public void info(Object message, Throwable t) {
        super.log(FQCN, Level.INFO, this.jobID + " - " + message, t);
    }

    public void error(Object message) {
        super.log(FQCN, Level.ERROR, this.jobID + " - " + message, null);
    }

    public void error(Object message, Throwable t) {
        super.log(FQCN, Level.ERROR, this.jobID + " - " + message, t);
    }

    public void debug(Object message) {
        super.log(FQCN, Level.DEBUG, this.jobID + " - " + message, null);
    }

    public void debug(Object message, Throwable t) {
        super.log(FQCN, Level.DEBUG, this.jobID + " - " + message, t);
    }

}
