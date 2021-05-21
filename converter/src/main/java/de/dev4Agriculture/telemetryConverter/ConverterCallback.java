package de.dev4Agriculture.telemetryConverter;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConverterCallback {
    private static Logger log = LogManager.getLogger(ConverterCallback.class);
    public void printLn(String entry){
        log.info(entry);

    }
}
