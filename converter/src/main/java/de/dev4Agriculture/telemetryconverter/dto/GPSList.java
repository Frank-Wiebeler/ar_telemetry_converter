package de.dev4Agriculture.telemetryconverter.dto;

import agrirouter.technicalmessagetype.Gps;
import com.google.protobuf.Timestamp;

import javax.swing.text.DateFormatter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GPSList {

    List<GPSListEntry> gpsEntryList;
    ConverterSettings converterSettings;
    public static DateFormat dateFormat = new SimpleDateFormat();


    public GPSList(){
        gpsEntryList = new ArrayList<>();
    }

    public void setSettings(ConverterSettings settings){
        this.converterSettings = settings;
        GPSListEntry.setSplitter(converterSettings.floatSplitter, converterSettings.columnSplitter);
    }

    public void add(GPSListEntry gpsListEntry) {
        this.gpsEntryList.add(gpsListEntry);
    }

    public String toCSVString(){
        String csvText =
                "timeStamp"+ converterSettings.columnSplitter +
                        "latitude"+ converterSettings.columnSplitter +
                        "longitude"+ converterSettings.columnSplitter +
                        "altitude"+ converterSettings.columnSplitter +
                        "status"+ converterSettings.columnSplitter +
                        "numberOfSatellites"+ converterSettings.columnSplitter +
                        "pdop" + converterSettings.columnSplitter +
                        "hdop\n";


        for (GPSListEntry gpsListEntry: gpsEntryList) {
            if( converterSettings.rawGPSPositions) {
                csvText = csvText + gpsListEntry.getRawCSVLine();
            } else {
                csvText = csvText + gpsListEntry.getCSVLine();
            }
        }

        return csvText;
    }

    public void cleanGPSData() {
        List<GPSListEntry> entriesToRemove = new ArrayList<>();
        for(GPSListEntry gpsListEntry : gpsEntryList){
            if(
                    gpsListEntry.timeStamp.getSeconds() == 0 ||
                            gpsListEntry.status == Gps.GPSList.GPSEntry.PositionStatus.D_ERROR ||
                            gpsListEntry.status == Gps.GPSList.GPSEntry.PositionStatus.D_NO_GPS ||
                            gpsListEntry.status == Gps.GPSList.GPSEntry.PositionStatus.UNRECOGNIZED){

                entriesToRemove.add(gpsListEntry);
            }
        }
        gpsEntryList.removeAll(entriesToRemove);
    }


    public void sortGPSData() {
        Collections.sort(gpsEntryList);
    }

    public void setDateFormat(DateFormat _dateFormat) {
        dateFormat = _dateFormat;
        GPSListEntry.setDateFormat(dateFormat);
    }
}
