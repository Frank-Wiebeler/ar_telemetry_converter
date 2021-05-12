package de.dev4Agriculture.telemetryConverter.Importer;

import agrirouter.technicalmessagetype.Gps;
import de.dev4Agriculture.telemetryConverter.dto.GPSList;
import de.dev4Agriculture.telemetryConverter.dto.GPSListEntry;
import org.aef.efdi.GrpcEfdi;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class EFDIImporter implements  DataImporter {


    private GrpcEfdi.TimeLog readProtobufFile(Path name) throws IOException {
        InputStream inputStream = new FileInputStream(name.toString());
        GrpcEfdi.TimeLog timeLog = GrpcEfdi.TimeLog.parseFrom(inputStream);
        System.out.println("Loaded EFDI File; Size of list: " + timeLog.getTimeList().size());
        return timeLog;
    }


    private GrpcEfdi.TimeLog readProtobufFile(InputStream inputStream) throws IOException {
        GrpcEfdi.TimeLog timeLog = GrpcEfdi.TimeLog.parseFrom(inputStream);
        System.out.println("Loaded File; Size of list: " + timeLog.getTimeList().size());
        return timeLog;
    }

    private GPSList convertProtobufListToGPSList(GrpcEfdi.TimeLog timeLogList) {
        GPSList gpsList = new GPSList();
        for ( GrpcEfdi.Time entry: timeLogList.getTimeList()) {
            GPSListEntry gpsListEntry = new GPSListEntry();
            gpsListEntry.timeStamp = entry.getStart();
            GrpcEfdi.Position position = entry.getPositionStart();
            gpsListEntry.altitude = position.getPositionUp();
            gpsListEntry.longitude = position.getPositionEast();
            gpsListEntry.latitude = position.getPositionNorth();
            gpsListEntry.numberOfSatellites = position.getNumberOfSatellites();
            gpsListEntry.pdop = position.getPdop();
            gpsListEntry.hdop = position.getHdop();
            switch (position.getPositionStatus()){
                case D_NO_GPS:
                    gpsListEntry.status = Gps.GPSList.GPSEntry.PositionStatus.D_NO_GPS;
                    break;
                case UNRECOGNIZED:
                    gpsListEntry.status = Gps.GPSList.GPSEntry.PositionStatus.UNRECOGNIZED;
                    break;
                case D_DGNSS:
                    gpsListEntry.status = Gps.GPSList.GPSEntry.PositionStatus.D_DGNSS;
                    break;
                case D_ERROR:
                    gpsListEntry.status = Gps.GPSList.GPSEntry.PositionStatus.D_ERROR;
                    break;
                case D_EST_DR_MODE:
                    gpsListEntry.status = Gps.GPSList.GPSEntry.PositionStatus.D_EST_DR_MODE;
                    break;
                case D_GNSS:
                    gpsListEntry.status = Gps.GPSList.GPSEntry.PositionStatus.D_GNSS;
                    break;
                case D_NOT_AVAILABLE:
                    gpsListEntry.status = Gps.GPSList.GPSEntry.PositionStatus.D_NOT_AVAILABLE;
                    break;
                case D_MANUAL_INPUT:
                    gpsListEntry.status = Gps.GPSList.GPSEntry.PositionStatus.D_MANUAL_INPUT;
                    break;
                case D_PRECISE_GNSS:
                    gpsListEntry.status = Gps.GPSList.GPSEntry.PositionStatus.D_PRECISE_GNSS;
                    break;
                case D_RTK_FINTEGER:
                    gpsListEntry.status = Gps.GPSList.GPSEntry.PositionStatus.D_RTK_FINTEGER;
                    break;
                case D_RTK_FLOAT:
                    gpsListEntry.status = Gps.GPSList.GPSEntry.PositionStatus.D_RTK_FLOAT;
                    break;
                case D_SIMULATE_MODE:
                    gpsListEntry.status = Gps.GPSList.GPSEntry.PositionStatus.D_SIMULATE_MODE;
                    break;
            }
            gpsList.add(gpsListEntry);
        }

        return gpsList;
    }


    public GPSList loadAndConvertToGPSList(Path importFilePath) throws IOException {
        GrpcEfdi.TimeLog gpsInfogpsList = this.readProtobufFile(importFilePath);
        return this.convertProtobufListToGPSList(gpsInfogpsList);
    }

    public GPSList loadAndConvertToGPSList(InputStream inputStream) throws IOException {
        GrpcEfdi.TimeLog gpsInfogpsList = this.readProtobufFile(inputStream);
        return this.convertProtobufListToGPSList(gpsInfogpsList);
    }

}
