package de.dev4Agriculture.telemetryConverter.dto;

import agrirouter.technicalmessagetype.Gps;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;

public class GPSList {
    private static Logger log = Logger.getLogger(GPSList.class);
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
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("timeStamp"+ converterSettings.columnSplitter +
                        "latitude"+ converterSettings.columnSplitter +
                        "longitude"+ converterSettings.columnSplitter +
                        "altitude"+ converterSettings.columnSplitter +
                        "status"+ converterSettings.columnSplitter +
                        "numberOfSatellites"+ converterSettings.columnSplitter +
                        "pdop" + converterSettings.columnSplitter +
                        "hdop\n");


        for (GPSListEntry gpsListEntry: gpsEntryList) {
            if( converterSettings.rawData) {
                stringBuilder.append(gpsListEntry.getRawCSVLine());
            } else {
                stringBuilder.append(gpsListEntry.getCSVLine());
            }
        }

        return stringBuilder.toString();
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
        int count= entriesToRemove.size();
        gpsEntryList.removeAll(entriesToRemove);
        log.info("Cleaned List from unknown GPS positions; deleted " + count + " entries");
    }


    public void sortGPSData() {
        Collections.sort(gpsEntryList);
        log.info("List sorted by date");
    }

    public void setDateFormat(DateFormat _dateFormat) {
        dateFormat = _dateFormat;
        GPSListEntry.setDateFormat(dateFormat);
    }
}
