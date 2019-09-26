package main.java.util;

import java.io.IOException;
import java.util.logging.*;

/**
 * Created by alfonce on 02/08/2017.
 */
public class LoggerUtil {

    static public void setup() throws IOException {
        //set global logger
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

        //suppress logging to console
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers.length > 1) {
            if (handlers[0] instanceof ConsoleHandler) {
                rootLogger.removeHandler(handlers[0]);
            }
        }

        FileHandler fileHandler = new FileHandler("app-log.%u.%g.txt",
                1024 * 1024, 10, true);

        //text formatter
        SimpleFormatter formatter = new SimpleFormatter();
        fileHandler.setFormatter(formatter);
        logger.addHandler(fileHandler);
    }
}
