package de.dev4Agriculture.telemetryConverter.dto;

import agrirouter.technicalmessagetype.Gps;
import com.google.protobuf.Timestamp;

import java.text.DateFormat;
import java.util.Date;

public class GPSListEntry implements  Comparable<GPSListEntry>{
    private static final double ALTITUDE_FACTOR = (1.0/1000);
    private static final double WGS84_POSITION_FACTOR = (1.0/10000000);
    private static final int MILLISECONDS_TO_SECONDS = 1000;
    public double latitude;
    public double longitude;
    public long altitude;
    public Gps.GPSList.GPSEntry.PositionStatus status;
    public Timestamp timeStamp;
    public int numberOfSatellites;
    public double pdop;
    public double hdop;

    public static String floatSplitter;
    public static String columnSplitter;
    public static DateFormat dateFormat;

    public static void setSplitter(String _floatSplitter, String _columnSplitter){
        floatSplitter =_floatSplitter;
        columnSplitter =_columnSplitter;
    }
    public static void setDateFormat(DateFormat _dateFormat){
        dateFormat = _dateFormat;
    }


    public int compareTo(GPSListEntry compare) {
        return (int)(compare.timeStamp.getSeconds() - this.timeStamp.getSeconds());
    }

    public static String convertGPSDouble(Double input){
        return Double.toString(input).replace(".",floatSplitter);
    }

    public String getCSVLine(){
        Date timeStampDate = new Date(timeStamp.getSeconds()*MILLISECONDS_TO_SECONDS);
        return dateFormat.format(timeStampDate) + columnSplitter +
                convertGPSDouble(latitude)  + columnSplitter +
                convertGPSDouble(longitude) +  columnSplitter +
                convertGPSDouble(altitude * ALTITUDE_FACTOR) +  columnSplitter +
                status.name() +  columnSplitter +
                numberOfSatellites +  columnSplitter +
                convertGPSDouble(pdop) +  columnSplitter +
                convertGPSDouble(hdop) +  columnSplitter +"\n";
    }

    public String getRawCSVLine(){
        return timeStamp.getSeconds() +  columnSplitter +
                convertGPSDouble(latitude) + columnSplitter +
                convertGPSDouble(longitude) +  columnSplitter +
                altitude +  columnSplitter +
                status.name() +  columnSplitter +
                numberOfSatellites +  columnSplitter +
                convertGPSDouble(pdop) +  columnSplitter +
                convertGPSDouble(hdop) +  columnSplitter + "\n";
    }
}