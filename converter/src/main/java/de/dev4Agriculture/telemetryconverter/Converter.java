package de.dev4Agriculture.telemetryconverter;

import agrirouter.technicalmessagetype.Gps;
import de.dev4Agriculture.telemetryconverter.dto.ConverterSettings;
import de.dev4Agriculture.telemetryconverter.dto.GPSList;
import de.dev4Agriculture.telemetryconverter.exceptions.CSVLockedException;
import de.dev4Agriculture.telemetryconverter.exceptions.EFDINotFoundException;
import de.dev4Agriculture.telemetryconverter.exceptions.GPSNotFoundException;
import org.aef.efdi.GrpcEfdi;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Converter {

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
        if(gpsList != null) {
            try {
                writeCSVFile(gpsList, exportFilePath);
            } catch (IOException e) {
                throw new CSVLockedException();
            }
        }
    }

    public static void convertEFDIDataFile(
            Path importFilePath,
            Path exportFilePath
    ) throws GPSNotFoundException, CSVLockedException, EFDINotFoundException {
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

}
