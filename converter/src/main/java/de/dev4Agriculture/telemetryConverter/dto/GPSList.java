package de.dev4Agriculture.telemetryConverter.dto;

import agrirouter.technicalmessagetype.Gps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        System.out.println("Cleaned List from unknown GPS positions; deleted " + count + " entries");
    }


    public void sortGPSData() {
        Collections.sort(gpsEntryList);
        System.out.println("List sorted by date");
    }

    public void setDateFormat(DateFormat _dateFormat) {
        dateFormat = _dateFormat;
        GPSListEntry.setDateFormat(dateFormat);
    }

    public List<GPSListEntry> getList() {
        return this.gpsEntryList;
    }
}
