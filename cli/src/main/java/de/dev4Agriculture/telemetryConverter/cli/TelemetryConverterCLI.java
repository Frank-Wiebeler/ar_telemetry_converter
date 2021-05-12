package de.dev4Agriculture.telemetryConverter.cli;


import de.dev4Agriculture.telemetryConverter.Exporter.CSVExporter;
import de.dev4Agriculture.telemetryConverter.Exporter.DataExporter;
import de.dev4Agriculture.telemetryConverter.Exporter.KMLExporter;
import de.dev4Agriculture.telemetryConverter.Importer.DataImporter;
import de.dev4Agriculture.telemetryConverter.Importer.EFDIImporter;
import de.dev4Agriculture.telemetryConverter.Importer.GPSInfoImporter;
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
        FORMAT_OUT
    }

    public enum InputFormat{
        UNKNOWN,
        EFDI,
        GPS,
        EFDI_ZIP
    }

    public enum OutputFormat{
        UNKNOWN,
        CSV,
        KML
    }

    public static void convert(String inputPath, String outputPath, String settingsPath,InputFormat inputFormat, OutputFormat outputFormat) throws SettingsNotFoundException, GPSNotFoundException, CSVLockedException, EFDINotFoundException, ZipNotLoadedException, NoExporterSpecificedException, NoImporterSpecifiedException {
        DataExporter dataExporter;
        DataImporter dataImporter;
        if (inputPath.equals("")) {
            log.error("No Input path provided");
        } else if (outputPath.equals("")) {
            log.error("No Output Path provided");
        }
        ConverterSettings settings;
        if (settingsPath.equals("")) {
            settings = ConverterSettings.getDefault();
        } else {
            settings = ConverterSettings.fromFile(settingsPath);
        }
        Converter.setSettings(settings);
        if( outputFormat.equals(OutputFormat.CSV)){
            dataExporter = new CSVExporter();
        } else if ( outputFormat.equals(OutputFormat.KML)){
            dataExporter = new KMLExporter();
        } else {
            throw new NoExporterSpecificedException();
        }
        if (inputFormat.equals(InputFormat.EFDI_ZIP)) {
            dataImporter = new EFDIImporter();
            Converter.convertEFDIZip(Paths.get(inputPath), Paths.get(outputPath), dataImporter, dataExporter);
        } else {
            if (inputFormat.equals(InputFormat.GPS)) {
                dataImporter = new GPSInfoImporter();
            } else if (inputFormat.equals(InputFormat.EFDI)) {
                dataImporter = new EFDIImporter();
            } else {
                throw new NoImporterSpecifiedException();
            }
            Converter.convert(Paths.get(inputPath), Paths.get(outputPath), dataImporter, dataExporter);

        }
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        boolean infoWasPrinted = false;
        String inputPath = "";
        String outputPath = "";
        String settingsPath = "";
        InputFormat inputFormat = InputFormat.UNKNOWN;
        OutputFormat outputFormat = OutputFormat.CSV;//To be backwards compatible, that's the default
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
                } else if (argument.equals("-fo")) {
                    nextParamType = NextParamType.FORMAT_OUT;
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
                        nextParamType = NextParamType.NONE;
                    } else if (argument.toUpperCase(Locale.ROOT).equals("GPS")){
                        inputFormat = InputFormat.GPS;
                        nextParamType = NextParamType.NONE;
                    } else if (argument.toUpperCase(Locale.ROOT).equals("EFDI_ZIP")){
                        inputFormat = InputFormat.EFDI_ZIP;
                        nextParamType = NextParamType.NONE;
                    } else {
                        inputFormat = InputFormat.UNKNOWN;
                        nextParamType = NextParamType.NONE;
                    }
            } else if(nextParamType.equals(NextParamType.FORMAT_OUT)){
                if(argument.toUpperCase(Locale.ROOT).equals("CSV")){
                    outputFormat = OutputFormat.CSV;
                    nextParamType = NextParamType.NONE;
                } else if (argument.toUpperCase(Locale.ROOT).equals("KML")){
                    outputFormat = OutputFormat.KML;
                    nextParamType = NextParamType.NONE;
                } else {
                    outputFormat = OutputFormat.UNKNOWN;
                    nextParamType = NextParamType.NONE;
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

        if(outputFormat == OutputFormat.UNKNOWN){
            log.error("Output format is wrong");
        }

        try {
            convert(inputPath,outputPath,settingsPath,inputFormat,outputFormat);
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
        } catch (NoImporterSpecifiedException e) {
            log.error("Error: No importer defined. Must be GPS,EFDI or EFDI_ZIP");
        } catch (NoExporterSpecificedException e) {
            log.error("Error: No exporter defined. Must be CSV or KML");
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