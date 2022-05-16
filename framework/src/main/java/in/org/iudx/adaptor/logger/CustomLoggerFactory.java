package in.org.iudx.adaptor.logger;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

public class CustomLoggerFactory implements LoggerFactory {

    private static String adaptorID;

    public CustomLoggerFactory(String adaptorID) {
        this.adaptorID = adaptorID;
    }

    public Logger makeNewLoggerInstance(String name) {
        return new CustomLogger(name, this.adaptorID);
    }
}
