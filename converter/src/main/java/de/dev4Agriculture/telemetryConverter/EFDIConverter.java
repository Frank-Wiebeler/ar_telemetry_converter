package de.dev4Agriculture.telemetryConverter;

import agrirouter.technicalmessagetype.Gps;
import de.dev4Agriculture.telemetryConverter.dto.GPSList;
import de.dev4Agriculture.telemetryConverter.dto.GPSListEntry;
import org.aef.efdi.GrpcEfdi;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class EFDIConverter {


    public static GrpcEfdi.TimeLog readProtobufFile(Path name) throws IOException {
        InputStream inputStream = new FileInputStream(name.toString());
        GrpcEfdi.TimeLog timeLog = GrpcEfdi.TimeLog.parseFrom(inputStream);
        System.out.println("Loaded EFDI File; Size of list: " + timeLog.getTimeList().size());
        return timeLog;
    }


    public static GrpcEfdi.TimeLog readProtobufFile(InputStream inputStream) throws IOException {
        GrpcEfdi.TimeLog timeLog = GrpcEfdi.TimeLog.parseFrom(inputStream);
        System.out.println("Loaded File; Size of list: " + timeLog.getTimeList().size());
        return timeLog;
    }

    public static GPSList convertProtobufListToGPSList(GrpcEfdi.TimeLog timeLogList) {
        GPSList gpsList = new GPSList();
        for ( GrpcEfdi.Time entry: timeLogList.getTimeList()) {
            GPSListEntry gpsListEntry = new GPSListEntry();
            gpsListEntry.timeStamp = entry.getStart();
            GrpcEfdi.Position position = entry.getPositionStart();
            if( position != null) {
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
            } else {
                gpsListEntry.altitude = 0;
                gpsListEntry.longitude = 0;
                gpsListEntry.latitude = 0;
                gpsListEntry.status = Gps.GPSList.GPSEntry.PositionStatus.D_NO_GPS;
                gpsListEntry.numberOfSatellites = 0;
                gpsListEntry.pdop = 0;
                gpsListEntry.hdop = 0;

            }
            gpsList.add(gpsListEntry);
        }

        return gpsList;
    }

}
