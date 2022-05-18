package in.org.iudx.adaptor.logger;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

public class CustomLoggerFactory implements LoggerFactory {

    private String adaptorID;

    public CustomLoggerFactory() {
        // when no adaptor id is passed
    }
    public CustomLoggerFactory(String adaptorID) {
        this.adaptorID = adaptorID;
    }

    public Logger makeNewLoggerInstance(String name) {
        if (this.adaptorID != null) {
            return new CustomLogger(name, this.adaptorID);
        } else {
            return new CustomLogger(name);
        }

    }
}
