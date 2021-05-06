package de.dev4Agriculture.telemetryConverter.cli;


import de.dev4Agriculture.telemetryConverter.exceptions.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import de.dev4Agriculture.telemetryConverter.Converter;
import de.dev4Agriculture.telemetryConverter.dto.ConverterSettings;

import java.nio.file.Paths;
import java.util.*;

public class TelemetryConverterCLI {
    public static Logger log = Logger.getLogger(TelemetryConverterCLI.class);
    public enum NextParamType {
        NONE,
        INPUT,
        OUTPUT,
        SETTINGS,
        FORMAT_IN,
    }

    public enum InputFormat{
        UNKNOWN,
        EFDI,
        GPS,
        EFDI_ZIP
    }

    public static void convert(String inputPath, String outputPath, String settingsPath,InputFormat inputFormat) throws SettingsNotFoundException, GPSNotFoundException, CSVLockedException, EFDINotFoundException, ZipNotLoadedException {
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
        } else if (inputFormat.equals(InputFormat.EFDI_ZIP)) {
            Converter.convertEFDIZip(Paths.get(inputPath),Paths.get(outputPath));
        } else {
            log.error("Unknown Input format");
        }
    }


    public static void main(String[] args) {
        BasicConfigurator.configure();
        boolean infoWasPrinted = false;
        String inputPath = "";
        String outputPath = "";
        String settingsPath = "";
        InputFormat inputFormat = InputFormat.UNKNOWN;
        NextParamType nextParamType = NextParamType.NONE;
        for (String argument : args) {
            if (nextParamType.equals(NextParamType.NONE)) {
                if (argument.equals("-i")) {
                    nextParamType = NextParamType.INPUT;
                } else if (argument.equals("-o")) {
                    nextParamType = NextParamType.OUTPUT;
                } else if (argument.equals("-s")) {
                    nextParamType = NextParamType.SETTINGS;
                } else if (argument.equals("-fi")) {
                    nextParamType = NextParamType.FORMAT_IN;
                } else if (argument.equals("-h")) {
                    nextParamType = NextParamType.NONE;
                    infoWasPrinted = true;
                    printHelp();
                } else if (argument.equals("-v")) {
                    nextParamType = NextParamType.NONE;
                    infoWasPrinted = true;
                    printVersion();
                } else {
                    System.out.println("Error in the command line syntax. Received entries: " + args.toString());
                }
            } else if(nextParamType.equals(NextParamType.INPUT)){
                    inputPath = argument;
                    nextParamType = NextParamType.NONE;
            } else if(nextParamType.equals(NextParamType.OUTPUT)){
                    outputPath = argument;
                    nextParamType = NextParamType.NONE;
            } else if(nextParamType.equals(NextParamType.SETTINGS)){
                    settingsPath = argument;
                    nextParamType = NextParamType.NONE;
            } else if(nextParamType.equals(NextParamType.FORMAT_IN)){
                    if(argument.toUpperCase(Locale.ROOT).equals("EFDI")){
                        inputFormat = InputFormat.EFDI;
                    } else if (argument.toUpperCase(Locale.ROOT).equals("GPS")){
                        inputFormat = InputFormat.GPS;
                    } else if (argument.toUpperCase(Locale.ROOT).equals("EFDI_ZIP")){
                        inputFormat = InputFormat.EFDI_ZIP;
                    } else {
                        inputFormat = InputFormat.UNKNOWN;
                    }
            }
        }

        if(inputPath.equals("") || outputPath.equals("")){
            if(infoWasPrinted) {
                return;
            } else {
                log.error("InputPath or OutputPath missing");
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
        } catch (ZipNotLoadedException e) {
            log.error("Error: EFDI Zip could not be loaded");
        }


    }

    private static void printHelp() {
        System.out.println("Agrirouter Telemetry Data converter \n\n Copyright 2021 \n\n dev4Agriculture\n\n" +
                "This tool converts gps:info, efdi:timelog and zipfiles including efdi:timelog to csv\n\n" +
                "Command Line parameters:" +
                "* -h: Print the help (basically this text)" +
                "* -v: Print the version" +
                "* -i: Is followed by the input file\n" +
                "* -o: Is followed by the output file or path\n" +
                "* -s: Is followed by the settings file\n" +
                "* -fi: Is followed by the Input format:\n" +
                "    * GPS: A .bin file including gps:info protobuf messages\n" +
                "    * EFDI: A .bin file including efdi:timelog messages\n" +
                "    * EFDI_ZIP: A .zip file including multiple efdi:timelog files. Output needs to be a path \n" +
                "\n\n" +
                "Settings.json example:" +
                "{\n" +
                "  \"dateFormat\": \"yyyy.MM.dd HH:mm:ss\",\n" +
                "  \"rawData\": false,\n" +
                "  \"sortData\": true,\n" +
                "  \"cleanData\": false,\n" +
                "  \"floatSplitter\": \",\",\n" +
                "  \"columnSplitter\": \";\"\n" +
                "}\n\n\n" +
                "" +
                "For further detail, check \n\n " +
                "* https://github.com/Frank-Wiebeler/ar_telemetry_converter \n" +
                "* https://www.dev4Agriculture.de" +
                "* https://www.my-agrirouter.com");
    }


    public static void printVersion(){
        System.out.println("V1.1_2021-05-06");
    }
}