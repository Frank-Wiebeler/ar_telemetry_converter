package de.dev4Agriculture.telemetryConverter;


import org.apache.log4j.Logger;

public class ConverterCallback {
    public static Logger log = Logger.getLogger(ConverterCallback.class);
    public void printLn(String entry){
        log.info(entry);

    }
}
