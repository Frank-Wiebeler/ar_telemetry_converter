package de.dev4Agriculture.telemetryConverter;

import agrirouter.technicalmessagetype.Gps;
import de.dev4Agriculture.telemetryConverter.Exporter.CSVExporter;
import de.dev4Agriculture.telemetryConverter.Exporter.DataExporter;
import de.dev4Agriculture.telemetryConverter.Exporter.KMLExporter;
import de.dev4Agriculture.telemetryConverter.Importer.DataImporter;
import de.dev4Agriculture.telemetryConverter.Importer.EFDIImporter;
import de.dev4Agriculture.telemetryConverter.Importer.GPSInfoImporter;
import de.dev4Agriculture.telemetryConverter.dto.ConverterSettings;
import de.dev4Agriculture.telemetryConverter.dto.GPSList;
import de.dev4Agriculture.telemetryConverter.exceptions.CSVLockedException;
import de.dev4Agriculture.telemetryConverter.exceptions.EFDINotFoundException;
import de.dev4Agriculture.telemetryConverter.exceptions.GPSNotFoundException;
import de.dev4Agriculture.telemetryConverter.exceptions.ZipNotLoadedException;
import org.aef.efdi.GrpcEfdi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Converter {
    private static DateFormat dateFormat;
    private static DataImporter dataImporter;
    private static DataExporter dataExporter;

    private static ConverterSettings settings = ConverterSettings.getDefault();

    public static void setSettings(ConverterSettings converterSettings) {
        settings = converterSettings;
        dateFormat=  new SimpleDateFormat(settings.dateFormat);
    }



    public static void formatGPSList(GPSList gpsList){
        gpsList.setSettings(settings);
        gpsList.setDateFormat(dateFormat);
        if( settings.sortData) {
            gpsList.sortGPSData();
        }

        if( settings.cleanData){
            gpsList.cleanGPSData();
        }
    }

    public static void convert(
            Path importFilePath,
            Path exportFilePath,
            DataImporter dataImporter,
            DataExporter dataExporter
    ) throws GPSNotFoundException, CSVLockedException {

        dataExporter.setConverterSettings(settings);
        GPSList gpsList;
        try {
            gpsList = dataImporter.loadAndConvertToGPSList(importFilePath);
            formatGPSList(gpsList);
        } catch (FileNotFoundException e) {
            throw new GPSNotFoundException();
        } catch (IOException e) {
            throw new GPSNotFoundException();
        }

        try {
            dataExporter.export(gpsList, exportFilePath);
        } catch (IOException e) {
            throw new CSVLockedException();
        }

    }

    public static void convertEFDIZip(
            Path importFilePath,
            Path exportFilePath,
            DataImporter dataImporter,
            DataExporter dataExporter
    ) throws CSVLockedException, EFDINotFoundException, ZipNotLoadedException {
        GPSList gpsList;
        GrpcEfdi.TimeLog timeLog;
        ZIPEFDIHandler zipefdiHandler;
        try {
            zipefdiHandler = new ZIPEFDIHandler(importFilePath.toString());
        } catch (IOException ioException) {
            throw new ZipNotLoadedException();
        }
        File outFolder = exportFilePath.toFile();
        if(!outFolder.exists()){
            if(!outFolder.mkdirs()){
                outFolder = exportFilePath.toFile();//Not sure if we need to assign it again to update the status, but better safe than sorry
                if(!outFolder.exists()) {
                    System.out.println("ERROR: Root folder to extract convert the files could not be created. Please choose a differen path");
                    return;
                }
            }
        }

        if ( zipefdiHandler.count() > 0){
            while (zipefdiHandler.nextZipEFDIEntry()){
                try {
                    gpsList = dataImporter.loadAndConvertToGPSList(zipefdiHandler.getZipEFDIEntryStream());
                    formatGPSList(gpsList);
                    if( settings.sortData) {
                        gpsList.sortGPSData();
                    }

                    if( settings.cleanData){
                        gpsList.cleanGPSData();
                    }
                } catch (FileNotFoundException e) {
                    throw new EFDINotFoundException();
                } catch (IOException e) {
                    throw new EFDINotFoundException();
                }

                try {
                    dataExporter.setConverterSettings(settings);
                    dataExporter.export(gpsList, Paths.get(exportFilePath.toString(),zipefdiHandler.getCSVName()));
                } catch (IOException e) {
                    throw new CSVLockedException();
                }
            }
        }

    }

    public static void convertGPS2CSV(Path inputPath, Path outputPath) throws GPSNotFoundException, CSVLockedException {
        DataImporter dataImporter = new GPSInfoImporter();
        DataExporter dataExporter = new CSVExporter();
        convert(inputPath,outputPath,dataImporter,dataExporter);
    }

    public static void convertGPS2KML(Path inputPath, Path outputPath) throws GPSNotFoundException, CSVLockedException {
        DataImporter dataImporter = new GPSInfoImporter();
        DataExporter dataExporter = new KMLExporter();
        convert(inputPath,outputPath,dataImporter,dataExporter);
    }
    public static void convertEFDI2CSV(Path inputPath, Path outputPath) throws GPSNotFoundException, CSVLockedException {
        DataImporter dataImporter = new EFDIImporter();
        DataExporter dataExporter = new CSVExporter();
        convert(inputPath,outputPath,dataImporter,dataExporter);
    }
    public static void convertEFDI2KML(Path inputPath, Path outputPath) throws GPSNotFoundException, CSVLockedException {
        DataImporter dataImporter = new EFDIImporter();
        DataExporter dataExporter = new KMLExporter();
        convert(inputPath,outputPath,dataImporter,dataExporter);
    }


    public static String getVersion(){
        return "V1.4_2021-05-20";
    }

}
