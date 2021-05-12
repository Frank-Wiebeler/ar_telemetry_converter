package de.dev4Agriculture.telemetryConverter.Exporter;

import de.dev4Agriculture.telemetryConverter.Converter;
import de.dev4Agriculture.telemetryConverter.dto.ConverterSettings;
import de.dev4Agriculture.telemetryConverter.dto.GPSList;
import de.dev4Agriculture.telemetryConverter.dto.GPSListEntry;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class CSVExporter implements DataExporter {
    private static org.apache.log4j.Logger log = Logger.getLogger(Converter.class);
    private ConverterSettings converterSettings;

    public void setConverterSettings(ConverterSettings converterSettings){
        this.converterSettings  = converterSettings;
    }

    private String toCSVString(GPSList gpsEntryList){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("timeStamp"+ converterSettings.columnSplitter);
        stringBuilder.append("latitude"+ converterSettings.columnSplitter);
        stringBuilder.append("longitude"+ converterSettings.columnSplitter);
        stringBuilder.append("altitude"+ converterSettings.columnSplitter);
        stringBuilder.append("status"+ converterSettings.columnSplitter);
        stringBuilder.append("numberOfSatellites"+ converterSettings.columnSplitter);
        stringBuilder.append("pdop" + converterSettings.columnSplitter);
        stringBuilder.append("hdop\n");


        for (GPSListEntry gpsListEntry: gpsEntryList.getList()) {
            if( converterSettings.rawData) {
                stringBuilder.append(gpsListEntry.getRawCSVLine());
            } else {
                stringBuilder.append(gpsListEntry.getCSVLine());
            }
        }

        return stringBuilder.toString();
    }


    public void export(GPSList gpsList, Path filePath) throws IOException {
        String fileContent = this.toCSVString(gpsList);
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toString()));

        writer.write(fileContent);
        writer.close();
        log.info("Export successfully written");
    }

}
