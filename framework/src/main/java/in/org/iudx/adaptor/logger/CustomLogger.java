package in.org.iudx.adaptor.logger;
import org.apache.log4j.*;


public class CustomLogger extends Logger {

    static String FQCN = CustomLogger.class.getName() + ".";
    static String jobID;

    public CustomLogger(String name, String jobID) {
        super(name);
        this.jobID = jobID;
    }

    public static Logger getLogger(Class name) {
        StackTraceElement trace[] = Thread.currentThread().getStackTrace();
        return Logger.getLogger(name.getName(), new CustomLoggerFactory(trace[trace.length - 2].getClassName()));
    }

    public void info(Object message) {
        super.log(FQCN, Level.INFO, this.jobID + " - " + message, null);
    }

}