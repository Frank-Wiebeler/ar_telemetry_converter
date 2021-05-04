package de.devAgriculture.telemetryConverter.cli;


import de.dev4Agriculture.telemetryconverter.exceptions.EFDINotFoundException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import de.dev4Agriculture.telemetryconverter.Converter;
import de.dev4Agriculture.telemetryconverter.GPSInfoConverter;
import de.dev4Agriculture.telemetryconverter.dto.ConverterSettings;
import de.dev4Agriculture.telemetryconverter.exceptions.CSVLockedException;
import de.dev4Agriculture.telemetryconverter.exceptions.GPSNotFoundException;
import de.dev4Agriculture.telemetryconverter.exceptions.SettingsNotFoundException;

import java.io.*;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class TelemetryConverterCLI {
    public static Logger log = Logger.getLogger(TelemetryConverterCLI.class);
    public enum NextParamType {
        NONE,
        INPUT,
        OUTPUT,
        SETTINGS,
        FORMAT_IN
    }

    public enum InputFormat{
        UNKNOWN,
        EFDI,
        GPS
    }

    public static void convert(String inputPath, String outputPath, String settingsPath,InputFormat inputFormat) throws SettingsNotFoundException, GPSNotFoundException, CSVLockedException, EFDINotFoundException {
        if( inputPath.equals("")){
            log.error("No Input path provided");
        } else if ( outputPath.equals("")){
            log.error("No Output Path provided");
        }
        ConverterSettings settings;
        if(settingsPath.equals("")){
            settings = ConverterSettings.getDefault();
        } else {
            settings = ConverterSettings.fromFile(settingsPath);
        }
        Converter.setSettings(settings);
        if(inputFormat.equals(InputFormat.GPS)) {
            Converter.convertGPSDataFile(Paths.get(inputPath), Paths.get(outputPath));
        } else if( inputFormat.equals(InputFormat.EFDI)){
            Converter.convertEFDIDataFile(Paths.get(inputPath),Paths.get(outputPath));
        } else {
            log.error("Unknown Input format");
        }
    }


    public static void main(String[] args) {
        BasicConfigurator.configure();
        String inputPath = "";
        String outputPath = "";
        String settingsPath = "";
        InputFormat inputFormat = InputFormat.UNKNOWN;
        NextParamType nextParamType = NextParamType.NONE;
        for (String argument : args) {
            switch (nextParamType) {
                case NONE:
                    if (argument.equals("-i")) {
                        nextParamType = NextParamType.INPUT;
                    } else if (argument.equals("-o")) {
                        nextParamType = NextParamType.OUTPUT;
                    } else if (argument.equals("-s")){
                        nextParamType = NextParamType.SETTINGS;
                    }
                    break;
                case INPUT:
                    inputPath = argument;
                    nextParamType = NextParamType.NONE;
                    break;
                case OUTPUT:
                    outputPath = argument;
                    nextParamType = NextParamType.NONE;
                    break;
                case SETTINGS:
                    settingsPath = argument;
                    nextParamType = NextParamType.NONE;
                    break;
                case FORMAT_IN:
                    if(argument.toUpperCase(Locale.ROOT).equals("EFDI")){
                        inputFormat = InputFormat.EFDI;
                    } else if (argument.toUpperCase(Locale.ROOT).equals("GPS")){
                        inputFormat = InputFormat.GPS;
                    } else {
                        inputFormat = InputFormat.UNKNOWN;
                    }
            }

            try {
                convert(inputPath,outputPath,settingsPath,inputFormat);
            } catch (GPSNotFoundException e) {
                log.error("Error: GPS File not found");
            } catch (CSVLockedException e) {
                log.error("Error: CSV-File is locked. Is it opened, e.g. in Excel?");
            } catch (SettingsNotFoundException e) {
                log.error("Error: Settings could not be loaded");
            } catch (EFDINotFoundException e) {
                log.error("Error: EFDI could not be loaded");
            }        }


    }
}