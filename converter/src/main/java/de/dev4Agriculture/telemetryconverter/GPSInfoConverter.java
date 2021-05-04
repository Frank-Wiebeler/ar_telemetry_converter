package de.dev4Agriculture.telemetryconverter;

import agrirouter.technicalmessagetype.Gps;
import com.google.protobuf.Timestamp;
import de.dev4Agriculture.telemetryconverter.dto.ConverterSettings;
import de.dev4Agriculture.telemetryconverter.dto.GPSList;
import de.dev4Agriculture.telemetryconverter.dto.GPSListEntry;
import de.dev4Agriculture.telemetryconverter.exceptions.CSVLockedException;
import de.dev4Agriculture.telemetryconverter.exceptions.GPSNotFoundException;

import java.io.*;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class GPSInfoConverter {

  public static Gps.GPSList readProtobufFile(Path name) throws IOException {
    InputStream inputStream = new FileInputStream(name.toString());
    System.out.println("Could load file");
    Gps.GPSList gpsList = Gps.GPSList.parseFrom(inputStream);
    System.out.println("Loaded File; Size of list: " + gpsList.getGpsEntriesList().size());
    return gpsList;
  }


  public static GPSList convertProtobufListToGPSList(Gps.GPSList gpsInfoList) {
    GPSList gpsList = new GPSList();
    for ( Gps.GPSList.GPSEntry entry: gpsInfoList.getGpsEntriesList()
    ) {
      GPSListEntry gpsListEntry = new GPSListEntry();
      gpsListEntry.altitude = entry.getPositionUp();
      gpsListEntry.longitude = entry.getPositionEast();
      gpsListEntry.latitude = entry.getPositionNorth();
      gpsListEntry.status = entry.getPositionStatus();
      gpsListEntry.timeStamp = entry.getGpsUtcTimestamp();
      gpsListEntry.numberOfSatellites = entry.getNumberOfSatellites();
      gpsListEntry.pdop = entry.getPdop();
      gpsListEntry.hdop = entry.getHdop();
      gpsList.add(gpsListEntry);
    }

    return gpsList;
  }
}
