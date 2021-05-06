package de.dev4Agriculture.telemetryConverter;

import agrirouter.technicalmessagetype.Gps;
import de.dev4Agriculture.telemetryConverter.dto.ConverterSettings;
import de.dev4Agriculture.telemetryConverter.dto.GPSList;
import de.dev4Agriculture.telemetryConverter.exceptions.CSVLockedException;
import de.dev4Agriculture.telemetryConverter.exceptions.EFDINotFoundException;
import de.dev4Agriculture.telemetryConverter.exceptions.GPSNotFoundException;
import de.dev4Agriculture.telemetryConverter.exceptions.ZipNotLoadedException;
import org.aef.efdi.GrpcEfdi;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Converter {
    private static Logger log = Logger.getLogger(Converter.class);
    private static DateFormat dateFormat;

    private static ConverterSettings settings = ConverterSettings.getDefault();

    public static void setSettings(ConverterSettings converterSettings) {
        settings = converterSettings;
        dateFormat=  new SimpleDateFormat(settings.dateFormat);
    }



    public static void writeCSVFile(GPSList gpsList, Path filePath) throws IOException {
        String fileContent = gpsList.toCSVString();
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toString()));

        writer.write(fileContent);
        writer.close();
        log.info("Export successfully written");
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

    public static void convertGPSDataFile(
            Path importFilePath,
            Path exportFilePath
    ) throws GPSNotFoundException, CSVLockedException {
        Gps.GPSList gpsInfogpsList;
        GPSList gpsList;
        try {
            gpsInfogpsList = GPSInfoConverter.readProtobufFile(importFilePath);
            gpsList = GPSInfoConverter.convertProtobufListToGPSList(gpsInfogpsList);
            formatGPSList(gpsList);
        } catch (FileNotFoundException e) {
            throw new GPSNotFoundException();
        } catch (IOException e) {
            throw new GPSNotFoundException();
        }

        try {
            writeCSVFile(gpsList, exportFilePath);
        } catch (IOException e) {
            throw new CSVLockedException();
        }

    }

    public static void convertEFDIDataFile(
            Path importFilePath,
            Path exportFilePath
    ) throws CSVLockedException, EFDINotFoundException {
        GPSList gpsList;
        GrpcEfdi.TimeLog timeLog;
        try {
            timeLog = EFDIConverter.readProtobufFile(importFilePath);
            gpsList = EFDIConverter.convertProtobufListToGPSList( timeLog);
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
            writeCSVFile(gpsList, exportFilePath);
        } catch (IOException e) {
            throw new CSVLockedException();
        }


    }

    public static void convertEFDIZip(
            Path importFilePath,
            Path exportFilePath
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
            outFolder.mkdirs();
        }

        if ( zipefdiHandler.count() > 0){
            while (zipefdiHandler.nextZipEFDIEntry()){
                try {
                    timeLog = EFDIConverter.readProtobufFile(zipefdiHandler.getZipEFDIEntryStream());
                    gpsList = EFDIConverter.convertProtobufListToGPSList( timeLog);
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
                    writeCSVFile(gpsList, Paths.get(exportFilePath.toString(),zipefdiHandler.getCSVName()));
                } catch (IOException e) {
                    throw new CSVLockedException();
                }
            }
        }

    }

}
