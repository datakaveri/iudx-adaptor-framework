package in.org.iudx.adaptor.logger;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class CustomLogger extends Logger {

    private final String fqcn = CustomLogger.class.getName();
    private String jobID = null;

    public CustomLogger(String name) {
        super(name);
    }

    public CustomLogger(String name, String jobID) {
        super(name);
        this.jobID = jobID;
    }

    public static Logger getLogger(Class name, String jobID) {
        return Logger.getLogger(name.getName(), new CustomLoggerFactory(jobID));
    }

    public static Logger getLogger(Class name) {
        return Logger.getLogger(name.getName(), new CustomLoggerFactory());
    }

    @Override
    public void info(Object message) {
        if (this.jobID != null) {
            super.log(fqcn, Level.INFO, this.jobID + " - " + message, null);
        } else {
            super.log(fqcn, Level.INFO, message, null);
        }
    }

    @Override
    public void info(Object message, Throwable t) {
        if (this.jobID != null) {
            super.log(fqcn, Level.INFO, this.jobID + " - " + message, t);
        } else {
            super.log(fqcn, Level.INFO, message, t);
        }
    }

    @Override
    public void error(Object message) {
        if (this.jobID != null) {
            super.log(fqcn, Level.ERROR, this.jobID + " - " + message, null);
        } else {
            super.log(fqcn, Level.ERROR, message, null);
        }
    }

    @Override
    public void error(Object message, Throwable t) {
        if (this.jobID != null) {
            super.log(fqcn, Level.ERROR, this.jobID + " - " + message, t);
        } else {
            super.log(fqcn, Level.ERROR, message, t);
        }
    }

    @Override
    public void debug(Object message) {
        if (this.jobID != null) {
            super.log(fqcn, Level.DEBUG, this.jobID + " - " + message, null);
        } else {
            super.log(fqcn, Level.DEBUG, message, null);
        }
    }

    @Override
    public void debug(Object message, Throwable t) {
        if (this.jobID != null) {
            super.log(fqcn, Level.DEBUG, this.jobID + " - " + message, t);
        } else {
            super.log(fqcn, Level.DEBUG, message, t);
        }
    }

}
