package de.dev4Agriculture.telemetryConverter.Importer;

import agrirouter.technicalmessagetype.Gps;
import de.dev4Agriculture.telemetryConverter.dto.GPSList;
import de.dev4Agriculture.telemetryConverter.dto.GPSListEntry;
import org.aef.efdi.GrpcEfdi;

import java.io.*;
import java.nio.file.Path;

public class GPSInfoImporter implements  DataImporter {

  private Gps.GPSList readProtobufFile(Path name) throws IOException {
    InputStream inputStream = new FileInputStream(name.toString());
    System.out.println("Could load file");
    Gps.GPSList gpsList = Gps.GPSList.parseFrom(inputStream);
    System.out.println("Loaded GPS Info File; Size of list: " + gpsList.getGpsEntriesList().size());
    return gpsList;
  }

  private Gps.GPSList readProtobufFile(InputStream inputStream) throws IOException {
    Gps.GPSList gpsList = Gps.GPSList.parseFrom(inputStream);
    System.out.println("Loaded File; Size of list: " + gpsList.getGpsEntriesList().size());
    return gpsList;
  }

  private GPSList convertProtobufListToGPSList(Gps.GPSList gpsInfoList) {
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


  public GPSList loadAndConvertToGPSList(Path importFilePath) throws IOException {
    Gps.GPSList gpsInfogpsList = this.readProtobufFile(importFilePath);
    return this.convertProtobufListToGPSList(gpsInfogpsList);
  }

  public GPSList loadAndConvertToGPSList(InputStream inputStream) throws IOException {
    Gps.GPSList gpsInfogpsList = this.readProtobufFile(inputStream);
    return this.convertProtobufListToGPSList(gpsInfogpsList);
  }
}
